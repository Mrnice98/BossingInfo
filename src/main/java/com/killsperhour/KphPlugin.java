/*
 * Copyright (c) 2020, MrNice98
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.killsperhour;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;


@PluginDescriptor(
        name = "KPH Tracker",
        description = "Shows various things like Kills per hour for the boss you are killing",
        tags = {"PVM", "kills per hour"}
)
public class KphPlugin extends Plugin
{
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private KphOverlay overlay;

    @Inject
    private KphConfig config;

    @Inject
    private ChatCommandManager chatCommandManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private Client client;

    @Inject
    private KphBossInfo kphBossInfo;

    @Inject
    private KphSpecialMethods sMethods;

    @Inject
    private ClientToolbar clientToolbar;

    private KphPanel panel;

    private KphInfobox infobox;


    private Instant startTime;
    private Instant totalSessionStart = Instant.now();
    private Instant timeoutStart;
    private Instant pauseStart;
    private Instant killTimerStart;


    final String supremeMessage = "Your Dagannoth Supreme kill count is:";
    final String rexMessage = "Your Dagannoth Rex kill count is:";
    final String primeMessage = "Your Dagannoth Prime kill count is:";

    private int totalTime;
    private int delayTicks;
    private int timerOffset;
    private int totalBossKillTime;
    private int pauseTime;
    private int totalKillTime;
    private int firstKillTime;
    private int attkCount;
    private final int[] cmRegions = {13138, 13137, 13139, 13141, 13136, 13145, 13393, 13394, 13140, 13395, 13397};
    private final int[] regGauntletRegion = {7512};
    private final int[] cGauntletRegion = {7768};
    private final int[] gargBossRegion = {6727};

    private boolean configState;
    boolean cacheHasInfo;

    private NPC lastAttackedBoss;
    private NPC currentNPC;

    NPC lastValidBoss;

    String message;
    String sessionNpc;
    String currentBoss;

    Instant primeStart;
    Instant rexStart;
    Instant supremeStart;

    double killsPerHour;
    int averageKillTime;
    int killsThisSession;
    int totalSessionTime;
    int timeSpentIdle;
    int primeAttkTimout = 21;
    int rexAttkTimout = 21;
    int supremeAttkTimout = 21;

    boolean paused;

    Map<String, Integer> bossIcon = new HashMap<String, Integer>();
    ArrayList<String> noDisplayBosses = new ArrayList<String>();
    ArrayList<String> displayBosses = new ArrayList<String>();




    //need to make a way to reset the first kill time if the boss is not hit within 10-15sec or something, with an exception for corp and sire.




//                                                OPERATIONAL METHODS USED TO POWER THE PLUGIN
//######################################################################################################################################################


    //Does what it says
    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged)
    {
        if(configChanged.getKey().equals("KPH Calc Method"))
        {
            panel.updateKphMethod();
        }

        if(configChanged.getKey().equals("Dagannoth Selector"))
        {
            if(sessionNpc != null)
            {
                if(sessionNpc.equals("Dagannoth Kings") || sessionNpc.equals("Dagannoth Prime") || sessionNpc.equals("Dagannoth Supreme") || sessionNpc.equals("Dagannoth Rex"))
                {
                    sessionEnd();
                }
            }
        }

        if(configChanged.getKey().equals("Display Infobox"))
        {
            if(config.renderInfobox())
            {
                addInfoBox();
            }
            else
            {
                infoBoxManager.removeInfoBox(infobox);
            }
        }
        //can add a method where if the config option is changed for account for banking, update the total time accordingly so i dont need to reset the session
    }

    //INTEGRITY MAINTAINER
    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
       if(gameStateChanged.getGameState() == GameState.HOPPING || gameStateChanged.getGameState() == GameState.LOGGING_IN)
       {
           attkCount = 0;
           delayTicks = 0;
       }
       //this is to make sure garg boss times are gathered correctly
       if(gameStateChanged.getGameState() == GameState.LOADING)
       {
           int[] currentRegions = client.getMapRegions();
           if(currentRegions == gargBossRegion && client.isInInstancedRegion())
           {
               if (!sessionNpc.equals("Grotesque Guardians"))
               {
                   sessionEnd();
                   sessionNpc = "Grotesque Guardians";
               }
           }
       }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        Player player = client.getLocalPlayer();
        assert player != null;

        //stops plugin from reading chat when paused
        if(paused)
        {
            return;
        }
        //this delay is needed as if a player hops worlds, the chat is reloaded and the kills per session would 2x if not for the delay to stop the plugin from reading them.
        if(delayTicks < 3)
        {
            return;
        }
        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE || chatMessage.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION)
        {
            this.message = chatMessage.getMessage();
            integrityCheck();
            bossKc();
            bossKillTime();
            calcKillsPerHour();
        }
    }


    //private final List<String> noDisplayBosses = Arrays.asList("Giant Mole", "the nightmare");
    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
    {
        if (!paused)
        {
            if (hitsplatApplied.getActor() instanceof NPC)
            {
                NPC npc = (NPC)hitsplatApplied.getActor();
                if(hitsplatApplied.getHitsplat().isMine())
                {
                    lastAttackedBoss = npc;
                    if(noDisplayBosses.contains(lastAttackedBoss.getName()))
                    {
                        lastValidBoss = lastAttackedBoss;
                        //includes a param which resets their attk timeout if they are the one being hit
                        sMethods.dagTimeClac();
                        attkCount++;
                        if (attkCount == 1)
                        {
                            currentNPC = lastValidBoss;
                            setKillTimeStart();
                        }

                        if (currentNPC.getName() != null && lastValidBoss.getName() != null)
                        {
                            if(!currentNPC.getName().equals(lastValidBoss.getName()))
                            {
                                attkCount = 1;
                                setKillTimeStart();
                                currentNPC = lastValidBoss;
                            }
                        }
                    }
                }
            }
        }
    }

    public void setKillTimeStart()
    {
        if(sMethods.dagChecker())
        {
            killTimerStart = sMethods.dagTimeClac();
        }
        else
        {
            killTimerStart = Instant.now();
        }
    }


    //resets the attkcount when the boss you are attking dies
    int stoper;
    public void bossKilled()
    {
        if(lastValidBoss != null)
        {
            if(lastValidBoss.isDead() && stoper == 0)
            {
                if(lastValidBoss.getId() != NpcID.KALPHITE_QUEEN_963)
                {
                    attkCount = 0;
                }
                stoper = 1;
            }
            if(!lastValidBoss.isDead() && stoper == 1)
            {
                stoper = 0;
            }
        }
    }

    //UPDATER / FETCHER
    private int ticks;

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        primeAttkTimout++;
        rexAttkTimout++;
        supremeAttkTimout++;

        delayTicks++;
        ticks++;
        bossKilled();

        if(lastValidBoss != null)
        {
            if(sMethods.dagChecker())
            {
                sMethods.dagTimeClear();
            }
        }

      if(ticks == 2)
      {
          ticks = 0;
          if(killsThisSession > 0 && !paused && config.timeoutTime() != 0)
          {
              sessionTimeoutTimer();
          }
      }

    }






//END SECTION
//###############################################################################################################################################################



//                                                                  INITIALIZERS / BUILDERS
//##################################################################################################################################################################
    public void addInfoBox()
    {
        if(config.renderInfobox() && sessionNpc != null)
        {
            infoBoxManager.removeInfoBox(infobox);
            BufferedImage image = itemManager.getImage(bossIcon.get(sessionNpc));
            infobox = new KphInfobox(image, this,config, OverlayPosition.DETACHED);
            infoBoxManager.addInfoBox(infobox);
        }

    }

    private void buildSidePanel()
    {
        this.panel = (KphPanel) this.injector.getInstance(KphPanel.class);
        this.panel.sidePanelInitializer();
        BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "/icon.png");
        NavigationButton navButton = NavigationButton.builder().tooltip("KPH Tracker").icon(icon).priority(10).panel(this.panel).build();
        this.clientToolbar.addNavigation(navButton);
    }


//END SECTION
//###############################################################################################################################################################




//                               BOSS TIME KILL TIME IDENTIFIERS FOR BOSSES WHICH DISPLAY TIME
//##################################################################################################################################################################

    //gets the killtime of the last kill as displayed in the chat and if selected calls for the banking time to be calculated
    public int bossKillTime()
    {

        //FIGHT DURATION CHAT IDENTIFIER, FOR BOSSESS WHO OUTPUT IN THAT FORMAT.
        if(message.contains("Fight duration:"))
        {
            //the "Grotesque Guardians" is the only boss which outputs time before kill count and also uses fight duration
            //therefore we want Fight duration to act normally if not kill GG's
            if(sessionNpc.equals("Grotesque Guardians"))
            {
                displayFirstIncrementerAndInitializer();
            }
            return chatDisplayKillTimeGetter();
        }

        //Chambers identifier
        // all bossess who output with duration before kc message need to follow same example as chambers
        if(message.contains("Congratulations - your raid is complete!"))
        {
            message = message.substring(message.indexOf("Duration:</col>") + 15);
            displayFirstIncrementerAndInitializer();
            return chatDisplayKillTimeGetter();
        }

        if(message.contains("Corrupted challenge duration:"))
        {
            displayFirstIncrementerAndInitializer();
            return chatDisplayKillTimeGetter();
        }

        if(message.contains("Challenge duration:"))
        {
            displayFirstIncrementerAndInitializer();
            return chatDisplayKillTimeGetter();
        }

        if(message.contains("Theatre of Blood total completion time: "))
        {
            message = message.substring(message.indexOf("time: ") + 5);
            displayFirstIncrementerAndInitializer();
            return chatDisplayKillTimeGetter();
        }

        else
            return 0;



    }

    //Increments the kill count for display first bosses and initializes the session if kills = 1. This is for bosses who display time first then KC.
    public void displayFirstIncrementerAndInitializer()
    {
        killsThisSession++;
        if(killsThisSession == 1)
        {
            sessionInitializer();
        }
    }




//END OF SECTION
//##########################################################################################################################################






//                                               BELOW IS THE BOSS IDENTIFICATION SECTION
//#########################################################################################################################################

    //CHAT DISPLAY BOSSES LISTED BELOW
    //keeps track of the kills done during the session and calls the session checker to make sure the session is still valid
    public void bossKc()
    {
        //ZULRAH IDENTIFIER
        if(message.contains("Your Zulrah kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Zulrah"; //must make sure all boss identifiers set session npc before the sessioninitalizer
                sessionInitializer();
            }
            currentBoss = "Zulrah";
            sessionChecker();

        }

        //VORKATH IDENTIFER
        if(message.contains("Your Vorkath kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Vorkath";
                sessionInitializer();
            }
            currentBoss = "Vorkath";
            sessionChecker();

        }

        //HYDRA IDENTIFER
        if(message.contains("Your Alchemical Hydra kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
              sessionNpc = "Hydra";
              sessionInitializer();
            }
            currentBoss = "Hydra";
            sessionChecker();

        }

        //GARG IDENTIFER
        if(message.contains("Your Grotesque Guardians kill count is:"))
        {
            updateSessionInfoCache();
            if(killsThisSession == 1)
            {
                sessionNpc = "Grotesque Guardians";
                sessionInitializer();
            }
            currentBoss = "Grotesque Guardians";
            sessionChecker();

        }


        //CORRUPTED GAUNTLET IDENTIFER
        if(message.contains("Your Corrupted Gauntlet completion count is:"))
        {
            updateSessionInfoCache();
            if(killsThisSession == 1)
            {
              sessionNpc =  "Corrupted Gauntlet";
              sessionInitializer();
            }
            currentBoss = "Corrupted Gauntlet";
            sessionChecker();

        }

        //GAUNTLET IDENTIFER
        if(message.contains("Your Gauntlet completion count is:"))
        {
            updateSessionInfoCache();
            if(killsThisSession == 1)
            {
                sessionNpc = "Gauntlet";
                sessionInitializer();
            }
            currentBoss = "Gauntlet";
            sessionChecker();

        }

        //NIGHTMARE IDENTIFER
        if(message.contains("Your Nightmare kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Nightmare";
                sessionInitializer();
            }
            currentBoss = "Nightmare";
            sessionChecker();

        }

        //both chambers dont have kills++ as it is added when time is displayed to allow for accurate calculation, the session inittlalizer has also been moved to same place
        //CHAMBERS IDENTIFER
        if(message.contains("Your completed Chambers of Xeric count is:"))
        {
            updateSessionInfoCache();
            if(killsThisSession == 1)
            {
                sessionNpc = "Chambers";
                sessionInitializer();
            }
            currentBoss = "Chambers";
            sessionChecker();

        }


        //When doing a CM directly after a chambers that will cause a miss read
        //CM CHAMBERS IDENTIFER
        if(message.contains("Your completed Chambers of Xeric Challenge Mode count is:"))
        {
            updateSessionInfoCache();
            if(killsThisSession == 1)
            {
                sessionNpc = "CM Chambers";
                sessionInitializer();
            }
            currentBoss = "CM Chambers";
            sessionChecker();

        }

        //THEATER IDENTIFER
        if(message.contains("Your completed Theatre of Blood count is:"))
        {
            updateSessionInfoCache();
            if(killsThisSession == 1)
            {
                sessionNpc = "Theater";
                sessionInitializer();
            }
            currentBoss = "Theater";
            sessionChecker();

        }

        //JAD IDENTIFER
        if(message.contains("Your TzTok-Jad kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "TzTok-Jad";
                sessionInitializer();
            }
            currentBoss = "TzTok-Jad";
            sessionChecker();

        }

        //ZUK IDENTIFER
        if(message.contains("Your TzKal-Zuk kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "TzKal-Zuk";
                sessionInitializer();
            }
            currentBoss = "TzKal-Zuk";
            sessionChecker();

        }





//------------------------------------------------NON-DISPLAY BOSSES BELOW----------------------------------------------------------------------

//Below are bosses which do not display a kill time, there time is calced purely from the timer and is handled in there if satement
        //i can definately put this into a switch statement at some point or at very least i can make this code cleaner, by putting the majority of the body into a mehtod

        //as i have it set up right now everything is working reletively well.

        //GIANT MOLE IDENTIFIER --- edited with potental changes
        if(message.contains("Your Giant Mole kill count is:"))
        {
            updateSessionInfoCache();
            //prevKillNumber = killsThisSession;

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Giant Mole";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Giant Mole";
            noDisplayKillTimeGetter();
            sessionChecker();
        }


        //Sarachnis IDENTIFIER
        if(message.contains("Your Sarachnis kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Sarachnis";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Sarachnis";
            noDisplayKillTimeGetter();
            sessionChecker();
        }


        //ABYSSAL SIRE BOSS IDENTIFIER
        if(message.contains("Your Abyssal Sire kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Abyssal Sire";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Abyssal Sire";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Zilyana BOSS IDENTIFIER
        if(message.contains("Your Commander Zilyana kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Zilyana";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Zilyana";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Bandos
        if(message.contains("Your General Graardor kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "General Graardor";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "General Graardor";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Arma
        if(message.contains("Your Kree'arra kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Kree'arra";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Kree'arra";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //KRIL / ZAMMY
        if(message.contains("Your K'ril Tsutsaroth kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "K'ril Tsutsaroth";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "K'ril Tsutsaroth";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Kraken
        if(message.contains("Your Kraken kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Kraken";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Kraken";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //THERMY
        if(message.contains("Your Thermonuclear Smoke Devil kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Thermy";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Thermy";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //CERBERUS
        if(message.contains("Your Cerberus kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Cerberus";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Cerberus";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //KING BLACK DRAGON
        if(message.contains("Your King Black Dragon kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "King Black Dragon";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "King Black Dragon";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Scorpia
        if(message.contains("Your Scorpia kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Scorpia";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Scorpia";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Chaos Fanatic
        if(message.contains("Your Chaos Fanatic kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Chaos Fanatic";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Chaos Fanatic";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Crazy Archaeologist
        if(message.contains("Your Crazy Archaeologist kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Crazy Archaeologist";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Crazy Archaeologist";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Chaos Elemental
        if(message.contains("Your Chaos Elemental kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Chaos Elemental";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Chaos Elemental";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Vet'ion
        if(message.contains("Your Vet'ion kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Vet'ion";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Vet'ion";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Venenatis
        if(message.contains("Your Venenatis kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Venenatis";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Venenatis";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Barrows
        if(message.contains("Your Barrows chest count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Barrows";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Barrows";
            noDisplayKillTimeGetter();
            sessionChecker();
        }


        //Deranged Archaeologist
        if(message.contains("Your Deranged Archaeologist kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Deranged Archaeologist";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Deranged Archaeologist";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Kalphite Queen
        if(message.contains("Your Kalphite Queen kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Kalphite Queen";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Kalphite Queen";
            noDisplayKillTimeGetter();
            sessionChecker();
        }

        //Corporeal Beast
        if(message.contains("Your Corporeal Beast kill count is:"))
        {
            updateSessionInfoCache();

            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionNpc = "Corporeal Beast";
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);
            }

            currentBoss = "Corporeal Beast";
            noDisplayKillTimeGetter();
            sessionChecker();
        }


        //Daggonoth kings
        if((message.contains(rexMessage) || message.contains(primeMessage) || message.contains(supremeMessage)) && config.dksSelector() == KphConfig.DksSelector.Kings
        || (message.contains(rexMessage) && config.dksSelector() == KphConfig.DksSelector.Rex)
        || (message.contains(primeMessage) && config.dksSelector() == KphConfig.DksSelector.Prime)
        || (message.contains(supremeMessage) && config.dksSelector() == KphConfig.DksSelector.Supreme))
        {
            updateSessionInfoCache();
            killsThisSession++;

            if(killsThisSession == 1)
            {
                sessionNpc = "Dagannoth " + config.dksSelector().toString();
                sessionInitializer();
                firstKillTime = generalTimer(killTimerStart);

                System.out.println("dag first kill " + firstKillTime);
            }

            currentBoss = "Dagannoth " + config.dksSelector().toString();

            sMethods.dagTimeClear();

            noDisplayKillTimeGetter();
            sessionChecker();
        }







    }


    //sets the values at a start of a new session
    private void sessionInitializer()
    {
        totalSessionStart = Instant.now();
        configState = getBankConfigState();
        startTime = Instant.now();

        if(sessionNpc != null)
        {
            addInfoBox();
            panel.setBossIcon();
        }

    }


//END OF SECTION
//#############################################################################################################################################





//                                                         SETTERS AND CHECKERS
//##############################################################################################################################################


    //checks to make sure the boss you are killing has not changed
    public void sessionChecker()
    {
        timeoutStart = Instant.now();
        //session changed
        if (!sessionNpc.equals(currentBoss) || getBankConfigState() != configState)
        {
            sessionReset();
        }
    }

    //these are needed for bossess who output the time first in chat then there kc, without this times could get messed up when switching sessions
    public void integrityCheck()
    {
        //Guantlet check
        int[] currentRegions;
        if(message.contains("Time remaining:") && sessionNpc != null)
        {
            currentRegions = client.getMapRegions();

            if (!sessionNpc.equals("Gauntlet") && Arrays.equals(regGauntletRegion, currentRegions))
            {
                sessionEnd();
            }
            if(!sessionNpc.equals("Corrupted Gauntlet") && Arrays.equals(cGauntletRegion, currentRegions))
            {
                sessionEnd();
            }
        }

        //Chambers check
        if(message.contains("The raid has begun!") && sessionNpc != null && client.getPlane() == 3)
        {
            currentRegions = client.getMapRegions();

            if (!sessionNpc.equals("CM Chambers") && Arrays.equals(cmRegions, currentRegions))
            {
                sessionEnd();
            }
            else if(!sessionNpc.equals("Chambers") && !Arrays.equals(cmRegions, currentRegions))
            {
                sessionEnd();
            }
        }

        //Theater check
        if(message.contains("Wave 'The Maiden of Sugadinti' complete! Duration:") && sessionNpc != null)
        {
            if (!sessionNpc.equals("Theater"))
            {
                sessionEnd();
            }
        }

    }


    //resets the session when you change from one boss to another
    public void sessionReset()
    {
        if(config.outputOnChange())
        {
            sessionInfoOutputMessage();
        }
        reset();
        killsThisSession = 1;
        sessionNpc = currentBoss;

        if(noDisplayBosses.contains(sessionNpc))
        {
            firstKillTime = generalTimer(killTimerStart);
            totalTime = firstKillTime;
            totalKillTime = firstKillTime;
            killTimeMessage(currentBoss);
        }

        if(!noDisplayBosses.contains(sessionNpc))
        {
            totalTime = bossKillTime();
        }

        addInfoBox();

        panel.setBossIcon();

        startTime = Instant.now();
    }


    //ends the session setting all values to null / zero or equivelant.
    public void sessionEnd()
    {
        if(sessionNpc != null)
        {
            //Displays end of session stats in chat
            if(config.outputOnChange())
            {
                updateSessionInfoCache();
                sessionInfoOutputMessage();
            }
            reset();
            killsThisSession = 0;
            sessionNpc = null;
            currentBoss = null;
            totalTime = 0;
            totalBossKillTime = 0;

            totalKillTime = 0;

            infoBoxManager.removeInfoBox(infobox);

            panel.setSessionInfo();
        }

    }

    public void reset()
    {
        paused = false;
        pauseTime = 0;
        configState = getBankConfigState();
        timeSpentIdle = 0;
        timerOffset = 0;
        attkCount = 0;

        timeoutStart = Instant.now();
        totalBossKillTime = bossKillTime();
        totalSessionStart = Instant.now();
    }


    //these values are fetched before any other code is run when a kill happens, that means that if a session is siwtched these values will hold the info of the last session.
     int cachedSessionKills;
     String cachedAvgKillTime;
     String cachedKPH;
     String cachedIdleTime;
     String cachedSessionTime;
     String cachedSessionNpc;

    //this updates the cache variables used to store the info chatmessage output, when this is called it gets the inforation at time of run
    public void updateSessionInfoCache()
    {
        cacheHasInfo = true;
        cachedKPH = formatKPH();
        cachedSessionKills = killsThisSession;
        cachedAvgKillTime = avgKillTimeConverter();
        cachedIdleTime = timeConverter(timeSpentIdle);
        cachedSessionTime = timeConverter(totalSessionTime);
        cachedSessionNpc = sessionNpc;
    }


    //outputs the info from session info cache the chat when session is changed or info is called.
    public void sessionInfoOutputMessage()
    {
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Session Info").build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("-------------------------").build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("KPH: " + cachedKPH).build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Kills: " + cachedSessionKills).build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Avg Kill: " + cachedAvgKillTime).build());
        if(config.accountForIdle() && config.displayIdleTime())
        {
            chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Bank/Idle: " + cachedIdleTime).build());
        }
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Session Time: " + cachedSessionTime).build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("-------------------------").build());
    }

    //Command to output session info into chat if at least one kill has been done since plugin was turned on. commands added at bottem
    private void infoCommand(ChatMessage chatMessage, String message)
    {
        if(cacheHasInfo)
        {
            if(sessionNpc != null)
            {
                updateSessionInfoCache();
            }
            sessionInfoOutputMessage();
        }
    }

    //Command to end the session and if the option is selected will output session info.
    private void endCommand(ChatMessage chatMessage, String message)
    {
        sessionEnd();
    }

    //Command to pause the session
    private void pauseCommand(ChatMessage chatMessage, String message)
    {
       sessionPause();
    }

    //Command to resume the session
    private void resumeCommand(ChatMessage chatMessage, String message)
    {
        sessionResume();
    }

    public void sessionPause()
    {
        if(!paused && sessionNpc != null)
        {
            chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Session Paused").build());
            paused = true;
            pauseStart = Instant.now();
            panel.setBossNameColor();
        }
    }

    public void sessionResume()
    {
        if(paused)
        {
            paused = false;
            pauseTime += generalTimer(pauseStart);
            timeoutStart = Instant.now();
            attkCount = 0;
            chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Session Resumed").build());
        }
    }





//END SECTION
//###############################################################################################################################################################






//                                                        CALCULATORS AND GETTERS
//##################################################################################################################################################################

    //calculates the kills per hour
    public void calcKillsPerHour()
    {
        if (killsThisSession != 0)
        {
            averageKillTime = totalTime / killsThisSession;
        }
        if(averageKillTime == 0)
        {
            killsPerHour = 0;
        }
        else
        {
            killsPerHour = 3600D / averageKillTime;
        }

        panel.setSessionInfo();
    }

    public String formatKPH()
    {
        int kph;
        switch (config.kphMethod())
        {
            case PRECISE:
                DecimalFormat df = new DecimalFormat("#.#");
                killsPerHour = Double.parseDouble(df.format(killsPerHour));
                return String.valueOf(killsPerHour);

            case ROUNDED:
                kph = (int)(Math.round(killsPerHour));
                return String.valueOf(kph);

            case ROUND_UP:
                kph = (int)(Math.ceil(killsPerHour));
                return String.valueOf(kph);

            case TRADITIONAL:
                kph = (int)killsPerHour;
                return String.valueOf(kph);

            default:
                return String.valueOf(killsPerHour);

        }
    }


    //simply calcultes the time not spent killing a boss who DOES have a time display
    public void timeSpentIdle()
    {
        if(bossType() == 0)
        {
            timeSpentIdle = totalTime - totalKillTime;
        }
        if(bossType() == 1)
        {
            timeSpentIdle = totalTime - totalBossKillTime;
        }
    }

    //identifies weather or not a boss displays kill times in chat, type 1 = yes, type 0 = no.
    public int bossType()
    {
        if(displayBosses.contains(sessionNpc))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    //gets the current value of the config option to turn on banking adjustment (true / false)
    public boolean getBankConfigState()
    {
        return config.accountForIdle();
    }



    //gets the kill time as displayed in the chat
    public int getKillTime()
    {
        String minutes;
        String seconds;
        String hours = "0";

        String trimmedMessage = message.replaceFirst("<","");
        int startOfTime = trimmedMessage.indexOf(">");
        int lastOfTime = trimmedMessage.indexOf("<");
        String sub = trimmedMessage.substring(startOfTime + 1, lastOfTime);
        sub = sub.replace(":","");
        switch (sub.length())
        {
            case 4:
                minutes = sub.substring(0,2);
                seconds = sub.substring(2);
                break;

            case 5:
                hours = sub.substring(0,1);
                minutes = sub.substring(1,2);
                seconds = sub.substring(2);
                break;

            default:
                minutes = sub.substring(0,1);
                seconds = sub.substring(1);
                break;
        }
        return Integer.parseInt(seconds) + (Integer.parseInt(minutes) * 60) + (Integer.parseInt(hours) * 3600);
    }

    //gets the kill time as displayed in chat and saves the value to totalBossKillTime, this method is called when banking is being taken into account
    //keeps a running total of kill times for bosses who display it.
    public int getTotalBossKillTime()
    {
        totalBossKillTime += getKillTime();
        return totalBossKillTime;
    }

    //gets the kill time from the chat for bossess who display it.
    public int chatDisplayKillTimeGetter()
    {
        getTotalBossKillTime();

        if(config.accountForIdle())
        {
            if(killsThisSession == 1)
            {
                timerOffset = getKillTime();
            }
            totalTime = (generalTimer(startTime) + timerOffset) - pauseTime;
            timeSpentIdle();
            return totalTime;
        }

        else
        {
            totalTime += getKillTime();
            return getKillTime();
        }

    }

    public void noDisplayKillTimeGetter()
    {
        if(sessionNpc.equals(lastValidBoss.getName()) || sMethods.dagKingsCheck())
        {
            if(config.accountForIdle())
            {
                totalTime = (generalTimer(startTime) + firstKillTime) - pauseTime;
            }
            else
            {
                totalTime += generalTimer(killTimerStart);
            }

            totalKillTime += generalTimer(killTimerStart);
            attkCount = 0;
            timeSpentIdle();

            if(sMethods.dagKingsCheck())
            {
                killTimeMessage(currentNPC.getName());
            }
            else
            {
                killTimeMessage(currentBoss);
            }

        }
    }

    public void killTimeMessage(String boss)
    {
        if(config.displayKillTimes())
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", boss + " Fight Duration: <col=ff0000>" + timeConverter(generalTimer(killTimerStart)),"");
        }
    }


//END OF SECTION
//#####################################################################################################################################






//                                                          TIMER'S SECTION
//############################################################################################################################################

    //times the entire session starting from when the first kill happens, used to track bossess with and without display
    public int generalTimer(Instant start)
    {
        String elapsedFormated;
        Duration elapsed = Duration.between(start, Instant.now());
        final String formatString = "ss";
        elapsedFormated = DurationFormatUtils.formatDuration(elapsed.toMillis(), formatString, true);
        return Integer.parseInt(elapsedFormated);
    }



    //Tracks the total time you have been in a given session
    public void totalSessionTimer()
    {
        String elapsedFormated;
        Duration elapsed = Duration.between(totalSessionStart, Instant.now());
        final String formatString = "ss";
        elapsedFormated = DurationFormatUtils.formatDuration(elapsed.toMillis(), formatString, true);
        if(bossType() == 1)
        {
            totalSessionTime = Integer.parseInt(elapsedFormated) + timerOffset - pauseTime;
        }
        else
        {                                                          //changed from banking offset to test out new method
            totalSessionTime = Integer.parseInt(elapsedFormated) + firstKillTime - pauseTime;
        }


        panel.setSessionTimeLabel();

    }


    //this is ued to calculate and keep track of the session timeout time / time since last kill
    public void sessionTimeoutTimer()
    {
        Duration offsetTime = Duration.between(timeoutStart, Instant.now());
        String offsetTimeFormated;
        final String formatString = "mm";
        offsetTimeFormated = DurationFormatUtils.formatDuration(offsetTime.toMillis(), formatString, true);

        int timeoutCount = Integer.parseInt(offsetTimeFormated);
        int timeoutTime = config.timeoutTime();

        if(timeoutCount >= timeoutTime)
        {
            sessionEnd();
        }
    }



//SECTION END
//###################################################################################################################################







//                                               TIME CONVERSION SECTION
//###################################################################################################################################


    public String avgKillTimeConverter()
    {
        String seconds;
        String minutes;
        if(averageKillTime < 60)
        {
            seconds = String.format("%02d",averageKillTime);
            minutes = "00";
        }
        else
        {
            minutes = String.format("%02d",averageKillTime / 60);
            seconds = String.format("%02d",averageKillTime % 60);
        }
        return minutes + ":" + seconds;


    }

    public String timeConverter(int time)
    {
        String seconds;
        String minutes;
        String hours;

        if(time > 3600)
        {
            hours = String.format("%02d",time / 3600);
            minutes = String.format("%02d",(time % 3600) / 60);
            seconds = String.format("%02d",time % 60);
            return hours + ":" + minutes + ":" + seconds;

        }
        if(time < 60)
        {
            seconds = String.format("%02d",time);
            minutes = "00";
        }
        else
        {
            minutes = String.format("%02d",time / 60);
            seconds = String.format("%02d",time % 60);
        }
        return minutes + ":" + seconds;

    }


//SECTION END
//#######################################################################################################################################


    @Provides
    KphConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(KphConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        //if i want to add a chat command just register it like this, what comes after the :: is the method call.
        chatCommandManager.registerCommandAsync("!Info", this::infoCommand);
        chatCommandManager.registerCommandAsync("!End", this::endCommand);
        chatCommandManager.registerCommandAsync("!Pause", this::pauseCommand);
        chatCommandManager.registerCommandAsync("!Resume", this::resumeCommand);

        if(config.showSidePanel())
        {
            buildSidePanel();
        }

        kphBossInfo.registerBossLists();

        paused = false;
        cacheHasInfo = false;
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        sessionEnd();

        chatCommandManager.unregisterCommand("!Info");
        chatCommandManager.unregisterCommand("!End");
        chatCommandManager.unregisterCommand("!Pause");
        chatCommandManager.unregisterCommand("!Resume");

        infoBoxManager.removeInfoBox(infobox);
        overlayManager.remove(overlay);
    }
}
