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
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.LootManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.loottracker.LootRecordType;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;

import static net.runelite.client.RuneLite.RUNELITE_DIR;


@PluginDescriptor(
        name = "Bossing Info",
        description = "Tracks statistics such as kills per hour and time bossed at various bosses.",
        tags = {"PVM", "kills per hour","kph","boss","info","boss utilities","boss util","util","utilities","kill times"}
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
    private KphSpecialMethods sMethods;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private FileReadWriter fileRW;

    @Inject
    private KphBossGoalsOverlay kphBossGoalsOverlay;

    @Getter
    private KphPanel panel;

    @Inject
    private LootManager lootManager;

    @Inject
    private ClientThread clientThread;


    private KphInfobox infobox;

    private NavigationButton navButton;

    private BufferedImage icon;

    private Instant startTime;
    private Instant totalSessionStart = Instant.now();
    private Instant timeoutStart;
    private Instant pauseStart;
    private Instant killTimerStart;


    final String supremeMessage = "Your Dagannoth Supreme kill count is:";
    final String rexMessage = "Your Dagannoth Rex kill count is:";
    final String primeMessage = "Your Dagannoth Prime kill count is:";


    private int delayTicks;
    private int timerOffset;
    private int pauseTime;
    private int firstKillTime;
    private int attkCount;

    private final int pohPortalId = ObjectID.PORTAL_4525;

    private final int[] cmRegions = {13138, 13137, 13139, 13141, 13136, 13145, 13393, 13394, 13140, 13395, 13397};
    private final int[] regGauntletRegion = {7512};
    private final int[] cGauntletRegion = {7768};
    private final int[] gargBossRegion = {6727};
    private final int[] fightCaveRegion = {9551};
    private final int[] infernoRegion = {9043};


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
    Instant barrowsStart;
    Instant sireStart;
    Instant krakenStart;


    Collection<ItemStack> itemStacks;


    int lastKillTotalTime_0; //no-display bosses
    int lastKillTotalTime_1; //display bosses

    double killsPerHour;
    int totalTime;
    int averageKillTime;

    int killCount;
    int killsThisSession;
    int totalKillTime;
    int totalBossKillTime;
    int totalSessionTime;
    int timeSpentIdle;
    int primeAttkTimout = 21;
    int rexAttkTimout = 21;
    int supremeAttkTimout = 21;

    int lastAttkTimeout = 99999;

    int startKC;
    int endKC;



    // 0 = Take idle into account & 1 = Dont take idle into account
    @Getter
    @Setter
    int calcMode;
    boolean paused;


//                                                OPERATIONAL METHODS USED TO POWER THE PLUGIN
//######################################################################################################################################################


    String bossName;


    @Subscribe
    public void onLootReceived(final LootReceived event)
    {
        if (event.getType() != LootRecordType.NPC && event.getType() != LootRecordType.EVENT)
        {
            return;
        }

        if (killsThisSession == 1 && !fileRW.sessionItemDrops.isEmpty())
        {
            fileRW.sessionItemDrops.clear();
        }

        itemStacks = event.getItems();
        bossName = event.getName();
        fileRW.lootReceived();
    }





    //Does what it says
    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged)
    {
        switch (configChanged.getKey())
        {
            case "KPH Calc Method":
                panel.updateKphMethod();
                panel.updateBossGoalsPanel();
                break;

            case "Side Panel":
                if(config.showSidePanel())
                {
                    buildSidePanel();
                }
                else
                {
                    clientToolbar.removeNavigation(navButton);
                }
                break;

            case "Side Panel Position":
                if(config.showSidePanel())
                {
                    clientToolbar.removeNavigation(navButton);
                    navButton = NavigationButton.builder()
                            .tooltip("Bossing Info")
                            .icon(icon)
                            .priority(config.sidePanelPosition())
                            .panel(panel)
                            .build();
                    clientToolbar.addNavigation(navButton);;
                }
                break;

            case "Dagannoth Selector":
                if(sessionNpc != null)
                {
                    if(sessionNpc.equals("Dagannoth Kings")
                      || sessionNpc.equals("Dagannoth Prime")
                      || sessionNpc.equals("Dagannoth Supreme")
                      || sessionNpc.equals("Dagannoth Rex"))
                    {
                        sessionEnd();
                    }
                }
                break;

            case "Infobox":
                if(config.renderInfobox())
                {
                    addInfoBox();
                }
                else
                {
                    infoBoxManager.removeInfoBox(infobox);
                }
                break;

            case "Display relative kills":
                if(sessionNpc != null)
                {
                    panel.updateBossGoalsPanel();
                }
                break;

            case "Display Boss Goals Panel":
                panel.bossGoalsPanel.setVisible(config.displayBossGoalsPanel());
                break;

            case "Loot Display":
                if(!panel.fetchedInfoPanel.isShowing())
                {
                    clientThread.invoke(() -> panel.updateLootGrid(panel.lootDisplayMap()));
                }
                break;

        }

    }

    //INTEGRITY MAINTAINER
    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
        {
            File mainFolder = new File(RUNELITE_DIR,"bossing-info");
            File file = new File(mainFolder, String.valueOf(client.getAccountHash()));
            if (client.getUsername() == null && !file.exists())
            {
                chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("<col=ff0000>[Bossing Info]<col=707070>Please do one boss kill on old Java client to migrate your info for Jagex Accounts<col=ffff00>").build());
            }

        }

       if(gameStateChanged.getGameState() == GameState.HOPPING || gameStateChanged.getGameState() == GameState.LOGGING_IN)
       {
           attkCount = 0;
           delayTicks = 0;
           nullStartTimesForSpecialBosses();
       }

        if(gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
       {
           sessionPause();
       }
    }


    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {

        Player player = client.getLocalPlayer();

        if(player == null || delayTicks < 5)
        {
            return;
        }

        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE || chatMessage.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION || chatMessage.getType() == ChatMessageType.SPAM)
        {
            canRun = false;
            this.message = chatMessage.getMessage();
            chatMessageFilter();
            sMethods.sireTimeClac();
            if(paused)
            {
                return;
            }
            bossKc();
            bossKillTime();
            chatMessageQueue();
        }
    }

    String kcMessage;
    String timeMessage;
    public void chatMessageQueue()
    {
        if(kcMessage != null && timeMessage != null)
        {
            canRun = true;
            chatDisplayBossMethod(currentBossName);
            chatDisplayKillTimeGetter();
            calcKillsPerHour();

            kcMessage = null;
            timeMessage = null;

            //needs to be after nulling the msgs
            printCurrentKph();
        }

    }


    boolean autoResumed;

    public void chatMessageFilter()
    {
        if(paused)
        {
            for (KphBossInfo bossIdentifier : KphBossInfo.values())
            {
                if (message.contains(bossIdentifier.getKcIdentifier()))
                {
                    sessionResume();
                    autoResumed = true;
                    break;
                }

            }

            for (String message : KphBossInfo.timeMessages)
            {
                if (this.message.contains(message))
                {
                    sessionResume();
                    autoResumed = true;
                    break;
                }

            }

        }

    }

    //something like from when the first hit to last death animation ect and calculate using that.
    //count from in game drop messages matching booster or team-mates names

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
    {
        if (hitsplatApplied.getActor() instanceof NPC)
        {
            NPC npc = (NPC)hitsplatApplied.getActor();

            if(hitsplatApplied.getHitsplat().isMine())
            {
                lastAttackedBoss = npc;

                KphBossInfo kphBossInfo = KphBossInfo.find(lastAttackedBoss.getName());

                if(kphBossInfo != null && kphBossInfo.getDisplayType() == 0)
                {
                    lastAttkTimeout = 0;
                    lastValidBoss = lastAttackedBoss;

                    sMethods.dagTimeClac();

                    attkCount++;
                    if (attkCount == 1)
                    {
                        currentNPC = lastValidBoss;

                        setAttkTimeout();

                        setKillTimeStart();
                    }


                    if (currentNPC.getName() != null && lastValidBoss.getName() != null)
                    {
                        if(!currentNPC.getName().equals(lastValidBoss.getName()) && lastValidBoss.getId() != NpcID.VETION_6612)
                        {
                            attkCount = 1;
                            setKillTimeStart();
                            currentNPC = lastValidBoss;
                            setAttkTimeout();
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
            return;
        }
        if(sMethods.barrowsChecker())
        {
            killTimerStart = sMethods.barrowsTimeClac();
            return;
        }
        if(sMethods.sireChecker() && sireStart != null)
        {
            killTimerStart = sireStart;
            return;
        }
        if(sMethods.krakenChecker())
        {
            killTimerStart = sMethods.krakenTimeClac();
            return;
        }
        else
        {
            killTimerStart = Instant.now();
        }
    }


    int stoper = 0;
    public void bossKilled()
    {
        if(lastValidBoss != null)
        {
            if(lastValidBoss.isDead() && stoper == 0)
            {
                if(lastValidBoss.getId() != NpcID.KALPHITE_QUEEN_963 && lastValidBoss.getId() != NpcID.VETION_6612)
                {
                    sMethods.dagTimeClearTwo();
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


    int attkTimeout;

    public void setAttkTimeout()
    {
        KphBossInfo kphBossInfo = KphBossInfo.find(currentNPC.getName());
        attkTimeout = kphBossInfo.getAttkTimeout();
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

        if(attkCount > 0 || timesAreNotNull())
        {
            lastAttkTimeout++;

            if(lastAttkTimeout == attkTimeout)
            {
                attkTimedOut();
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

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        // Retrieve the spawned object
        final GameObject gameObject = event.getGameObject();
        // Try and see if the PoH exit portal is found (id PORTAL_4525) if it is that means the player is in a PoH
        if (gameObject.getId() == pohPortalId)
        {
            // The player is in a PoH. Check to see if they want to end session when in PoH
            if (config.endSessionPoH())
                sessionEnd();
        }
    }

    public boolean timesAreNotNull()
    {
        return barrowsStart != null || sireStart != null;
    }



    public void attkTimedOut()
    {
        attkCount = 0;
        if(krakenStart != null)
        {
            sMethods.krakenTimeClear();
        }
        if(barrowsStart != null)
        {
            sMethods.barrowsTimeClear();
        }
        if(sireStart != null)
        {
            sMethods.sireTimeClear();
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
            KphBossInfo kphBossInfo = KphBossInfo.find(sessionNpc);
            infoBoxManager.removeInfoBox(infobox);
            BufferedImage image = itemManager.getImage(kphBossInfo.getIcon());
            infobox = new KphInfobox(image, this,config, OverlayPosition.DETACHED);
            infoBoxManager.addInfoBox(infobox);
        }

    }

    private void buildSidePanel()
    {
        panel = (KphPanel) injector.getInstance(KphPanel.class);
        panel.sidePanelInitializer();
        icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
        navButton = NavigationButton.builder().tooltip("Bossing Info").icon(icon).priority(config.sidePanelPosition()).panel(panel).build();
        clientToolbar.addNavigation(navButton);
    }


//END SECTION
//###############################################################################################################################################################




//                               BOSS TIME KILL TIME IDENTIFIERS FOR BOSSES WHICH DISPLAY TIME
//##################################################################################################################################################################

    boolean canRun;

    //gets the killtime of the last kill as displayed in the chat and if selected calls for the banking time to be calculated
    public int bossKillTime()
    {
        //FIGHT DURATION CHAT IDENTIFIER, FOR BOSSESS WHO OUTPUT IN THAT FORMAT.
        if(message.contains("Fight duration:"))
        {
            message = message.substring(message.indexOf("Fight duration:"));
            timeMessage = message;
            return 0;
        }

        if(message.contains("Duration:"))
        {
            if(Arrays.equals(client.getMapRegions(), fightCaveRegion) || Arrays.equals(client.getMapRegions(), infernoRegion))
            {
                timeMessage = message;
                return 0;
            }
        }

        //Chambers identifier
        // all bossess who output with duration before kc message need to follow same example as chambers
        if(message.contains("Congratulations - your raid is complete!"))
        {
            message = message.substring(message.indexOf("Duration:</col>") + 15);

            timeMessage = message;
            return 0;
        }

        if(message.contains("Corrupted challenge duration:"))
        {

            timeMessage = message;
            return 0;
        }

        if(message.contains("Challenge duration:"))
        {
            timeMessage = message;
            return 0;
        }

        if(message.contains("Theatre of Blood total completion time: "))
        {
            message = message.substring(message.indexOf("time: ") + 5);

            timeMessage = message;
            return 0;
        }

        if (message.contains("Tombs of Amascut total completion time:")
        || message.contains("Tombs of Amascut: Expert Mode total completion time:"))
        {
            timeMessage = message;
            return 0;
        }


        else
            return 0;



    }


//END OF SECTION
//##########################################################################################################################################


    public void chatDisplayBossMethod(String bossName)
    {
        updateSessionInfoCache();

        canRun = true;
        killsThisSession++;

        if(killsThisSession == 1)
        {
            sessionNpc = bossName; //must make sure all boss identifiers set session npc before the session initalizer
            sessionInitializer();
        }
        currentBoss = bossName;
        sessionChecker();
        killCount = getKillCount();
    }

    public void nonChatDisplayBossMethod(String bossName)
    {
        updateSessionInfoCache();

        killsThisSession++;
        if(killsThisSession == 1)
        {
            sessionNpc = bossName;
            sessionInitializer();
            firstKillTime = generalTimer(killTimerStart);
        }

        currentBoss = bossName;
        //clears times for special bosses
        conditionalTimeClear();
        killCount = getKillCount();
        noDisplayKillTimeGetter();
        sessionChecker();
        canRun = true;

        timeMessage = null;
        kcMessage = null;
    }



//                                               BELOW IS THE BOSS IDENTIFICATION SECTION
//#########################################################################################################################################

    //CHAT DISPLAY BOSSES LISTED BELOW
    //keeps track of the kills done during the session and calls the session checker to make sure the session is still valid

    String currentBossName;

    public void bossKc()
    {


        for (KphBossInfo bossIdentifier : KphBossInfo.values())
        {
            if (message.contains(bossIdentifier.getKcIdentifier()))
            {
                if(bossIdentifier.getDisplayType() == 1)
                {
                    kcMessage = message;
                    currentBossName = bossIdentifier.getName();
                }
                else
                {
                    nonChatDisplayBossMethod(bossIdentifier.getName());
                    calcKillsPerHour();
                    printCurrentKph();
                }

                break;
            }

        }


//------------------------------------------------NON-DISPLAY BOSSES BELOW----------------------------------------------------------------------

        //Special case bosses

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
            }

            currentBoss = "Dagannoth " + config.dksSelector().toString();
            sMethods.dagTimeClear();
            killCount = getKillCount();
            noDisplayKillTimeGetter();
            sessionChecker();
            canRun = true;
        }

    }

    private void conditionalTimeClear()
    {
        switch (currentBoss)
        {
            case "Barrows":
                sMethods.barrowsTimeClear();
                break;

            case "Kraken":
                sMethods.krakenTimeClear();
                break;

            case "Abyssal Sire":
                sMethods.sireTimeClear();
                break;
        }
    }


    //sets the values at a start of a new session
    private void sessionInitializer()
    {
        totalSessionStart = Instant.now();
        startTime = Instant.now();

        if(sessionNpc != null)
        {
            addInfoBox();
            panel.setBossIcon(sessionNpc);
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
        if (!sessionNpc.equals(currentBoss))
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

        KphBossInfo kphBossInfo = KphBossInfo.find(sessionNpc);
        if(kphBossInfo.getDisplayType() == 0)
        {
            firstKillTime = generalTimer(killTimerStart);

            totalTime = firstKillTime;
            totalKillTime = firstKillTime;
            lastKillTotalTime_0 = firstKillTime;

            fastestKill = firstKillTime;

            if(lastValidBoss.getName() == null)
            {
                killTimeMessage(currentBoss);
            }

        }

        if(kphBossInfo.getDisplayType() == 1)
        {
            fastestKill = 999999;
            totalTime = bossKillTime();
        }

        addInfoBox();
        panel.setBossIcon(sessionNpc);
        startTime = Instant.now();

    }





    //ends the session setting all values to null / zero or equivelant.
    public void sessionEnd()
    {
        if(sessionNpc != null)
        {
            //Displays end of session stats in chat
            updateSessionInfoCache();
            if(config.outputOnChange())
            {
                sessionInfoOutputMessage();
            }


            reset();
            killsThisSession = 0;
            sessionNpc = null;
            currentBoss = null;
            totalTime = 0;
            totalBossKillTime = 0;
            totalKillTime = 0;
            killCount = 0;
            fastestKill = 9999999;

            infoBoxManager.removeInfoBox(infobox);
            panel.setSessionInfo();
            panel.progressBar.setDimmed(true);
            panel.progressBar.setForeground(new Color(88, 88, 88));
            panel.progressBar.setDimmedText("No Session");
            panel.progressBar.repaint();


        }

    }

    public void reset()
    {

        paused = false;
        calcMode = 0;
        pauseTime = 0;
        timeSpentIdle = 0;
        timerOffset = 0;
        attkCount = 0;

        //teststuff
        lastkillnumber = -1;

        panel.switchModeButton.setText("     Actual     ");
        panel.pauseResumeButton.setText("      Pause     ");

        nullStartTimesForSpecialBosses();

        fileRW.resetStartAndEndKc();

        timeoutStart = Instant.now();
        totalBossKillTime = bossKillTime();
        totalSessionStart = Instant.now();
    }

    public void nullStartTimesForSpecialBosses()
    {
        sireStart = null;
        primeStart = null;
        rexStart = null;
        supremeStart = null;
        barrowsStart = null;
        krakenStart = null;
    }


    //these values are fetched before any other code is run when a kill happens, that means that if a session is siwtched these values will hold the info of the last session.
     int cachedSessionKills;
     String cachedAvgKillTime;
     String cachedKPH;
     String cachedIdleTime;
     String cachedSessionTime;
     String cachedSessionNpc;
     String cachedFastestKill;

    //this updates the cache variables used to store the info chatmessage output, when this is called it gets the inforation at time of run
    public void updateSessionInfoCache()
    {

        if (!fileRW.sessionItemDrops.isEmpty())
        {
            fileRW.cachedItemDrops = fileRW.sessionItemDrops;
        }

        cacheHasInfo = true;
        cachedKPH = formatKPH();
        cachedSessionKills = killsThisSession;
        cachedAvgKillTime = avgKillTimeConverter();
        cachedIdleTime = timeConverter(timeSpentIdle);
        cachedSessionTime = timeConverter(totalSessionTime);
        cachedFastestKill = timeConverter(fastestKill);
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
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Idle Time: " + cachedIdleTime).build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Session Time: " + cachedSessionTime).build());
        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("-------------------------").build());
    }

    public void printCurrentKph()
    {
        if(config.printKphInChat())
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","KPH: " + "<col=ff0000>"+ formatKPH()+"</col>","");
            //chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Kph: " + formatKPH()).build());

        }
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

            panel.pauseResumeButton.setText("    Resume    ");

            pauseStart = Instant.now();
            panel.setBossNameColor();
            panel.progressBar.setForeground(new Color(167, 125, 38));
            panel.progressBar.setCenterLabel("Paused");
            panel.progressBar.repaint();

        }
    }

    public void sessionResume()
    {
        if(paused)
        {
            paused = false;

            panel.pauseResumeButton.setText("      Pause     ");

            pauseTime += generalTimer(pauseStart);
            timeoutStart = Instant.now();
            nullStartTimesForSpecialBosses();
            attkCount = 0;
            panel.currentBossNameLabel.setForeground(new Color(71, 226, 12));
            panel.progressBar.setForeground(new Color(91, 154, 47));
            panel.progressBar.setCenterLabel(panel.percentDone + "%");
            panel.updateBossGoalsPanel();
            panel.progressBar.repaint();
            chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).runeLiteFormattedMessage("Session Resumed").build());
        }
    }





//END SECTION
//###############################################################################################################################################################






//                                                        CALCULATORS AND GETTERS
//##################################################################################################################################################################


    int lastkillnumber;

    int totalTrackedTime;
    double overallKph;


    //calculates the kills per hour
    public void calcKillsPerHour()
    {
        //test stuff
        if (killsThisSession != 0)
        {
            KphBossInfo kphBossInfo = KphBossInfo.find(currentBoss);
            if(kphBossInfo == null)
            {
                return;
            }
            if(calcMode == 1)
            {
                switch (kphBossInfo.getDisplayType())
                {
                    case 0:
                        totalTime = totalKillTime;
                        break;
                    case 1:
                        totalTime = totalBossKillTime;
                        break;
                }
            }

            if(calcMode == 0)
            {
                switch (kphBossInfo.getDisplayType())
                {
                    case 0:
                        totalTime = lastKillTotalTime_0;
                        break;
                    case 1:
                        totalTime = lastKillTotalTime_1;
                        break;
                }
            }


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

        timeSpentIdle();

        panel.setSessionInfo();


        //set a boolen that is set when a kill is registered to allow this to run once.
        if(currentBoss != null)
        {
            if(canRun)
            {
                fileRW.createAndUpdate();
                panel.setHistoricalInfo();
                panel.updateBossGoalsPanel();
                canRun = false;
            }
        }
        lastkillnumber = killsThisSession;
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
        if(sessionNpc != null)
        {
            KphBossInfo kphBossInfo = KphBossInfo.find(sessionNpc);

            if(kphBossInfo.getDisplayType() == 0)
            {
                timeSpentIdle = totalTime - totalKillTime;
            }
            if(kphBossInfo.getDisplayType() == 1)
            {
                timeSpentIdle = totalTime - totalBossKillTime;
            }
        }
    }


    //gets the kill time as displayed in the chat
    public int getKillTime()
    {
        String minutes;
        String seconds;
        String hours = "0";

        String trimmedMessage = timeMessage.replaceFirst("<","");
        int startOfTime = trimmedMessage.indexOf(">");
        int lastOfTime = trimmedMessage.indexOf("<");
        String sub = trimmedMessage.substring(startOfTime + 1, lastOfTime);

        if(sub.contains("."))
        {
            int endOfUsefulTime = sub.indexOf(".");
            sub = sub.substring(0, endOfUsefulTime);
        }

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

    public int getKillCount()
    {
        KphBossInfo kphBossInfo = KphBossInfo.find(currentBoss);
        String message = this.message;

        if(kphBossInfo.getDisplayType() == 1)
        {
            message = kcMessage;
        }


        String trimmedMessage = message.replace("<col=ff0000>","");
        trimmedMessage =  trimmedMessage.replaceAll("[^0-9]", "");
        return Integer.parseInt(trimmedMessage);
    }

    int currentKill;
    int fastestKill = 99999999;

    public void fastestKill()
    {
        if(currentKill < fastestKill)
        {
            fastestKill = currentKill;
        }
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

        currentKill = getKillTime();
        fastestKill();

        if(killsThisSession == 1 || autoResumed)
        {
            timerOffset += getKillTime();
            autoResumed = false;
        }
        totalTime = (generalTimer(startTime) + timerOffset) - pauseTime;

        lastKillTotalTime_1 = totalTime;

        return totalTime;
    }



    public void noDisplayKillTimeGetter()
    {

        totalKillTime += generalTimer(killTimerStart);

        if(autoResumed && killsThisSession != 1)
        {
            firstKillTime += generalTimer(killTimerStart);
            autoResumed = false;
        }

        totalTime = (generalTimer(startTime) + firstKillTime) - pauseTime;

        lastKillTotalTime_0 = totalTime;

        attkCount = 0;

        if(lastValidBoss.getName() != null || sessionNpc.equals("Barrows"))
        {
            if(sMethods.dagKingsCheck())
            {
                killTimeMessage(currentNPC.getName());
            }
            else
            {
                killTimeMessage(currentBoss);
            }
        }

        currentKill = generalTimer(killTimerStart);
        fastestKill();

    }

    public void killTimeMessage(String boss)
    {
        if(config.displayKillTimes())
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", boss + " Fight Duration: <col=ff0000>" + timeConverter(generalTimer(killTimerStart)),"");
        }
    }


    public boolean isBossChatDisplay()
    {
        KphBossInfo kphBossInfo = KphBossInfo.find(currentBoss);
        return kphBossInfo.getDisplayType() == 1;
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

        KphBossInfo kphBossInfo = KphBossInfo.find(sessionNpc);
        if(kphBossInfo.getDisplayType() == 1)
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

        //take idle time into account
        calcMode = 0;
        paused = false;
        cacheHasInfo = false;
        overlayManager.add(overlay);
        overlayManager.add(kphBossGoalsOverlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        sessionEnd();

        chatCommandManager.unregisterCommand("!Info");
        chatCommandManager.unregisterCommand("!End");
        chatCommandManager.unregisterCommand("!Pause");
        chatCommandManager.unregisterCommand("!Resume");

        clientToolbar.removeNavigation(navButton);
        infoBoxManager.removeInfoBox(infobox);
        overlayManager.remove(overlay);
        overlayManager.remove(kphBossGoalsOverlay);
    }
}
