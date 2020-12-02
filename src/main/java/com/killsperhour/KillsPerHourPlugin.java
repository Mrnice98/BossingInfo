/*
 * Copyright (c) 2020, MrNice98
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
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
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;


@PluginDescriptor(
        name = "KPH Tracker",
        description = "Shows various things like Kills per hour for the boss you are killing",
        tags = {"PVM", "kills per hour"}
)
public class KillsPerHourPlugin extends Plugin
{
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private KillsPerHourOverlay overlay;

    @Inject
    private KillsPerHourConfig config;

    @Inject
    private ChatCommandManager chatCommandManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private Client client;


    private Instant secondKillTime;
    private Instant startTime;
    private Instant totalSessionStart = Instant.now();
    private Instant timeoutStart;
    private Instant pauseStart;

    private String message;
    private String sessionNpc;
    private final String supremeMessage = "Your Dagannoth Supreme kill count is:";
    private final String rexMessage = "Your Dagannoth Rex kill count is:";
    private final String primeMessage = "Your Dagannoth Prime kill count is:";

    private int totalTime;
    private int averageKillTime;
    private int delayTicks;
    private int timerOffset;
    private int bankingOffset;
    private int totalBossKillTime;
    private int pauseTime;
    private final int[] cmRegions = {13138, 13137, 13139, 13141, 13136, 13145, 13393, 13394, 13140, 13395, 13397};
    private final int[] regGauntletRegion = {7512};
    private final int[] cGauntletRegion = {7768};
    private final int[] gargBossRegion = {6727};

    private boolean sessionHasBeenPaused;
    private boolean configState;
    private boolean cacheHasInfo;

    String currentBoss;
    double killsPerHour;
    int killsThisSession;
    int totalSessionTime;
    int timeSpentBanking;
    boolean paused;




//                                                OPERATIONAL METHODS USED TO POWER THE PLUGIN
//######################################################################################################################################################


    //INTEGRITY MAINTAINER
    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
       if(gameStateChanged.getGameState() == GameState.HOPPING || gameStateChanged.getGameState() == GameState.LOGGING_IN)
       {
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



    //UPDATER / FETCHER
    private int ticks;

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        delayTicks++;
        ticks++;
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
        // all bossess who output with duration before kc message need to follow same example as chambers ***********
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

        if(message.contains("Theatre of Blood total completion time:"))
        {
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
                sessionInitializer();
                sessionNpc = "Zulrah";
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
                sessionInitializer();
                sessionNpc = "Vorkath";
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
                sessionInitializer();
                sessionNpc = "Hydra";
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
                sessionInitializer();
                sessionNpc = "Grotesque Guardians";
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
                sessionInitializer();
                sessionNpc = "Corrupted Gauntlet";
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
                sessionInitializer();
                sessionNpc = "Gauntlet";
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
                sessionInitializer();
                sessionNpc = "Nightmare";
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
                sessionInitializer();
                sessionNpc = "CM Chambers";
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
                sessionInitializer();
                sessionNpc = "Theater";
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
                sessionInitializer();
                sessionNpc = "TzTok-Jad";
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
                sessionInitializer();
                sessionNpc = "TzKal-Zuk";
            }
            currentBoss = "TzKal-Zuk";
            sessionChecker();

        }



//------------------------------------------------NON-DISPLAY BOSSES BELOW----------------------------------------------------------------------

//Below are bosses which do not display a kill time, there time is calced purely from the timer and is handled in there if satement
        //i can definately put this into a switch statement at some point or at very least i can make this code cleaner, by putting the majority of the body into a mehtod

        //GIANT MOLE IDENTIFIER
        if(message.contains("Your Giant Mole kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Giant Mole";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Giant Mole";
            sessionChecker();
        }


        //Sarachnis IDENTIFIER
        if(message.contains("Your Sarachnis kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Sarachnis";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Sarachnis";
            sessionChecker();
        }


        //ABYSSAL SIRE BOSS IDENTIFIER
        if(message.contains("Your Abyssal Sire kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Abyssal Sire";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Abyssal Sire";
            sessionChecker();
        }

        //Zilyana BOSS IDENTIFIER
        if(message.contains("Your Commander Zilyana kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Zilyana";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Zilyana";
            sessionChecker();
        }

        //Bandos
        if(message.contains("Your General Graardor kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "General Graardor";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "General Graardor";
            sessionChecker();
        }

        //Arma
        if(message.contains("Your Kree'arra kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Kree'arra";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Kree'arra";
            sessionChecker();
        }

        //KRIL / ZAMMY
        if(message.contains("Your K'ril Tsutsaroth kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "K'ril Tsutsaroth";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "K'ril Tsutsaroth";
            sessionChecker();
        }

        //Kraken
        if(message.contains("Your Kraken kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Kraken";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Kraken";
            sessionChecker();
        }

        //THERMY
        if(message.contains("Your Thermonuclear Smoke Devil kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Thermy";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Thermy";
            sessionChecker();
        }

        //CERBERUS
        if(message.contains("Your Cerberus kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Cerberus";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Cerberus";
            sessionChecker();
        }

        //KING BLACK DRAGON
        if(message.contains("Your King Black Dragon kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "King Black Dragon";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "King Black Dragon";
            sessionChecker();
        }

        //Scorpia
        if(message.contains("Your Scorpia kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Scorpia";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Scorpia";
            sessionChecker();
        }

        //Chaos Fanatic
        if(message.contains("Your Chaos Fanatic kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Chaos Fanatic";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Chaos Fanatic";
            sessionChecker();
        }

        //Crazy Archaeologist
        if(message.contains("Your Crazy Archaeologist kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Crazy Archaeologist";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Crazy Archaeologist";
            sessionChecker();
        }

        //Chaos Elemental
        if(message.contains("Your Chaos Elemental kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Chaos Elemental";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Chaos Elemental";
            sessionChecker();
        }

        //Vet'ion
        if(message.contains("Your Vet'ion kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Vet'ion";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Vet'ion";
            sessionChecker();
        }

        //Venenatis
        if(message.contains("Your Venenatis kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Venenatis";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Venenatis";
            sessionChecker();
        }

        //Barrows
        if(message.contains("Your Barrows chest count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Barrows";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Barrows";
            sessionChecker();
        }


        //Deranged Archaeologist
        if(message.contains("Your Deranged Archaeologist kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Deranged Archaeologist";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Deranged Archaeologist";
            sessionChecker();
        }

        //Kalphite Queen
        if(message.contains("Your Kalphite Queen kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Kalphite Queen";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Kalphite Queen";
            sessionChecker();
        }

        //Corporeal Beast
        if(message.contains("Your Corporeal Beast kill count is:"))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Corporeal Beast";
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Corporeal Beast";
            sessionChecker();
        }


        //Daggonoth kings
        if((message.contains(rexMessage) || message.contains(primeMessage) || message.contains(supremeMessage)) && config.dksSelector() == com.killsperhour.KillsPerHourConfig.DksSelector.Kings
        || (message.contains(rexMessage) && config.dksSelector() == com.killsperhour.KillsPerHourConfig.DksSelector.Rex)
        || (message.contains(primeMessage) && config.dksSelector() == com.killsperhour.KillsPerHourConfig.DksSelector.Prime)
        || (message.contains(supremeMessage) && config.dksSelector() == com.killsperhour.KillsPerHourConfig.DksSelector.Supreme))
        {
            updateSessionInfoCache();
            killsThisSession++;
            if(killsThisSession == 1)
            {
                sessionInitializer();
                sessionNpc = "Dagannoth " + config.dksSelector();
            }
            if(killsThisSession == 2)
            {
                secondKillTime = Instant.now();
                bankingOffset();
            }
            //handles kill times
            totalTime = (sessionTimer() + bankingOffset) - pauseTime;
            currentBoss = "Dagannoth " + config.dksSelector();
            sessionChecker();
        }







    }


    //UPDATE WITH BOSSES WHICH DISPLAY KILL TIMES

    //identifies weather or not a boss displays kill times in chat, type 1 = yes, type 0 = no.
    public int bossType()
    {
        //this list contains all bossess which give time in chat, it is assumed if the boss is not on this list that the boss does not display time in chat.
        ArrayList<String> displayBosses = new ArrayList<String>();
        displayBosses.add("Zulrah");
        displayBosses.add("Vorkath");
        displayBosses.add("Hydra");
        displayBosses.add("Grotesque Guardians");
        displayBosses.add("Corrupted Gauntlet");
        displayBosses.add("Gauntlet");
        displayBosses.add("Nightmare");
        displayBosses.add("Chambers");
        displayBosses.add("CM Chambers");
        displayBosses.add("Theater");
        displayBosses.add("TzTok-Jad");
        displayBosses.add("TzKal-Zuk");

        if(displayBosses.contains(sessionNpc))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }


    //sets the values at a start of a new session
    private void sessionInitializer()
    {
        totalSessionStart = Instant.now();
        configState = getBankConfigState();
        startTime = Instant.now();
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
        totalTime = bossKillTime();
        startTime = Instant.now();
    }


    //ends the session
    public void sessionEnd()
    {
        //Displays end of session stats in chat
        if(sessionNpc != null && config.outputOnChange())
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
    }

    public void reset()
    {
        paused = false;
        pauseTime = 0;
        sessionHasBeenPaused = false;
        configState = getBankConfigState();
        timeSpentBanking = 0;
        bankingOffset = 0;
        timerOffset = 0;
        timeoutStart = Instant.now();
        totalBossKillTime = bossKillTime();
        totalSessionStart = Instant.now();
    }


    //these values are fetched before any other code is run when a kill happens, that means that if a session is siwtched these values will hold the info of the last session.
    private int cachedSessionKills;
    private String cachedAvgKillTime;
    private String cachedKPH;
    private String cachedBankTime;
    private String cachedSessionTime;

    //this updates the cache variables used to store the info chatmessage output, when this is called it gets the inforation at time of run
    public void updateSessionInfoCache()
    {
        DecimalFormat df = new DecimalFormat("#.#");
        cacheHasInfo = true;
        cachedKPH = df.format(killsPerHour);
        cachedSessionKills = killsThisSession;
        cachedAvgKillTime = avgKillTimeConverter();
        cachedBankTime = timeConverter(timeSpentBanking);
        cachedSessionTime = timeConverter(totalSessionTime);
    }


    //outputs the info from session info cache the chat when session is changed or info is called.
    public void sessionInfoOutputMessage()
    {
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Session Info").build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("-------------------------").build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("KPH: " + cachedKPH).build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Kills: " + cachedSessionKills).build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Avg Kill: " + cachedAvgKillTime).build());
        if(config.accountForBank() && config.displayBankingTime())
        {
            chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Bank/Idle: " + cachedBankTime).build());
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
        if(!paused && sessionNpc != null)
        {
            chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Session Paused").build());
            sessionHasBeenPaused = true;
            paused = true;
            pauseStart = Instant.now();

        }
    }

    //Command to resume the session
    private void resumeCommand(ChatMessage chatMessage, String message)
    {
        if(paused)
        {
            paused = false;
            pauseTime += pauseTimer();
            timeoutStart = Instant.now();
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
    }


    //simply calcultes the time not spent killing a boss who DOES have a time display
    public void timeSpentBanking()
    {
        timeSpentBanking = totalTime - totalBossKillTime;
    }

    //gets the current value of the config option to turn on banking adjustment (true / false)
    public boolean getBankConfigState()
    {
        return config.accountForBank();
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

        if(config.accountForBank())
        {
            if(killsThisSession == 1)
            {
                timerOffset = getKillTime();
            }
            totalTime = (sessionTimer() + timerOffset) - pauseTime;
            timeSpentBanking();
            return totalTime;
        }

        else
        {
            totalTime += getKillTime();
            return getKillTime();
        }

    }


//END OF SECTION
//#####################################################################################################################################






//                                                          TIMER'S SECTION
//############################################################################################################################################

    //times the entire session starting from when the first kill happens, used to track bossess with and without display
    public int sessionTimer()
    {
        String elapsedFormated;
        Duration elapsed = Duration.between(startTime, Instant.now());
        final String formatString = "ss";
        elapsedFormated = DurationFormatUtils.formatDuration(elapsed.toMillis(), formatString, true);
        return Integer.parseInt(elapsedFormated);
    }

    public int pauseTimer()
    {
        String elapsedFormated;
        Duration elapsed = Duration.between(pauseStart, Instant.now());
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
        {
            totalSessionTime = Integer.parseInt(elapsedFormated) + bankingOffset - pauseTime;
        }

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


    //this is used to get the time it took from the first kill of a boss to the second kill of a boss
    //this is used when the boss dose not display kill time
    public void bankingOffset()
    {

        Duration offsetTime = Duration.between(startTime, secondKillTime);
        String offsetTimeFormated;
        final String formatString = "ss";
        offsetTimeFormated = DurationFormatUtils.formatDuration(offsetTime.toMillis(), formatString, true);

        if(sessionHasBeenPaused)
        {
            bankingOffset = Integer.parseInt(offsetTimeFormated) - pauseTime;
        }
        else
        {
            bankingOffset = Integer.parseInt(offsetTimeFormated);
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
    com.killsperhour.KillsPerHourConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(com.killsperhour.KillsPerHourConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        //if i want to add a chat command just register it like this, what comes after the :: is the method call.
        chatCommandManager.registerCommandAsync("!Info", this::infoCommand);
        chatCommandManager.registerCommandAsync("!End", this::endCommand);
        chatCommandManager.registerCommandAsync("!Pause", this::pauseCommand);
        chatCommandManager.registerCommandAsync("!Resume", this::resumeCommand);

        sessionHasBeenPaused = false;
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

        overlayManager.remove(overlay);
    }
}
