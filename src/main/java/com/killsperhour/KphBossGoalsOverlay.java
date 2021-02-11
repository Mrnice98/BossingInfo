package com.killsperhour;

import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.*;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

class KphBossGoalsOverlay extends OverlayPanel
{
    private final Client client;
    private final KphConfig config;
    private final KphPlugin plugin;
    private final FileReadWriter fileRW;

    private static final int BORDER_SIZE = 2;
    private static final int XP_AND_PROGRESS_BAR_GAP = 0;
    private static final int ICON_AND_INFO_GAP = 2;
    private static final Rectangle ICON_AND_INFO_COMPONENT_BORDER = new Rectangle(2, 1, 4, 0);
    private final PanelComponent iconAndInfoPanel = new PanelComponent();
    private final ItemManager itemManager;

    @Inject
    private KphBossGoalsOverlay(Client client, KphConfig config, KphPlugin plugin, ItemManager itemManager, FileReadWriter fileRW)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.fileRW = fileRW;
        panelComponent.setBorder(new Rectangle(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        iconAndInfoPanel.setBorder(ICON_AND_INFO_COMPONENT_BORDER);
        iconAndInfoPanel.setBackgroundColor(null);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Bossing info overlay"));
        this.createPanels();
    }

    private LineComponent topLine;
    private LineComponent bottomLine;
    private SplitComponent splitLineComponent;
    private ImageComponent imageComponent;
    private SplitComponent iconComponentSplitter;
    private ProgressBarComponent progressBarComponent;

    public static BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight)
    {
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = scaledImage.createGraphics();
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaledImage;
    }

    int killsDone;
    int killsLeft;
    int totalKillsToGet;
    double percentDone;
    double TTG;
    String timeTillGoal;

    public void converter()
    {

        killsDone = plugin.killCount - fileRW.startKc;

        totalKillsToGet = fileRW.endKc - fileRW.startKc;

        killsLeft = totalKillsToGet - killsDone;

        TTG = killsLeft / plugin.killsPerHour;

        percentDone = 100 * ((double) killsDone / (double) totalKillsToGet);

        timeTillGoal = plugin.timeConverter((int)(TTG * 3600));
    }

    private void createPanels()
    {

        String leftStr;
        String rightNum;
        String bottomLeftStr;
        String bottomRightNum;

        converter();

        boolean goalComplete = killsLeft <= 0;

        switch (config.topGoalOverlay())
        {

            case TTG:
                leftStr = "TTG:";
                if(goalComplete){rightNum = "00:00:00";}
                else
                rightNum = timeTillGoal;
                break;

            case KILLS_DONE:
                leftStr = "Kills Done:";
                if(goalComplete){rightNum = String.valueOf(totalKillsToGet);}
                else
                rightNum = String.valueOf(killsDone);
                break;

            case KILLS_LEFT:
                leftStr = "Kills Left:";
                if(goalComplete){rightNum = "0";}
                else
                rightNum = String.valueOf(killsLeft);
                break;

            default:
                leftStr = "KPH:";
                rightNum = String.valueOf(plugin.formatKPH());
                break;

        }

        switch (config.bottomGoalOverlay())
        {
            case KPH:
                bottomLeftStr = "KPH:";
                bottomRightNum = String.valueOf(plugin.formatKPH());
                break;

            case TTG:
                bottomLeftStr = "TTG:";
                if(goalComplete){bottomRightNum = "00:00:00";}
                else
                bottomRightNum = timeTillGoal;
                break;

            case KILLS_DONE:
                bottomLeftStr = "Kills Done:";
                if(goalComplete){bottomRightNum = String.valueOf(totalKillsToGet);}
                else
                bottomRightNum = String.valueOf(killsDone);
                break;

            default:
                bottomLeftStr = "Kills Left:";
                if(goalComplete){bottomRightNum = "0";}
                else
                bottomRightNum = String.valueOf(killsLeft);
                break;

        }


        topLine = LineComponent.builder()
                .left(leftStr)
                .right(rightNum)
                .build();

        bottomLine = LineComponent.builder()
                .left(bottomLeftStr)
                .right(bottomRightNum)
                .build();

        splitLineComponent = SplitComponent.builder()
                .first(topLine)
                .second(bottomLine)
                .orientation(ComponentOrientation.VERTICAL)
                .build();


        KphBossInfo kphBossInfo =  KphBossInfo.find(plugin.sessionNpc);

        if(kphBossInfo != null)
        {
            imageComponent = new ImageComponent(resizeImage((itemManager.getImage(kphBossInfo.getIcon())), 32, 28));
        }
        else
        {
            //cant be asked to rearrange this right now so the cabbage will stay.... for now.
            imageComponent = new ImageComponent(resizeImage((itemManager.getImage(ItemID.CABBAGE)), 32, 28));
        }


        iconComponentSplitter = SplitComponent.builder()
                .first(imageComponent)
                .second(splitLineComponent)
                .orientation(ComponentOrientation.HORIZONTAL)
                .gap(new Point(ICON_AND_INFO_GAP, 0))
                .build();

        iconAndInfoPanel.getChildren().add(iconComponentSplitter);

        progressBarComponent = new ProgressBarComponent();

        progressBarComponent.setBackgroundColor(new Color(61, 56, 49));

        if(plugin.paused)
        {
            progressBarComponent.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.TEXT_ONLY);
            progressBarComponent.setCenterLabel("Paused");
            progressBarComponent.setForegroundColor(new Color(173, 128, 29));
        }
        else
        {
            progressBarComponent.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.PERCENTAGE);
            progressBarComponent.setForegroundColor(new Color(91, 154, 47));
        }



        if(config.displayRelativeKills())
        {
            progressBarComponent.setLeftLabel(String.valueOf(0));
            progressBarComponent.setRightLabel(String.valueOf(totalKillsToGet));
        }
        else
        {
            progressBarComponent.setLeftLabel(String.valueOf(fileRW.startKc));
            progressBarComponent.setRightLabel(String.valueOf(fileRW.endKc));
        }

        if(goalComplete)
        {
            progressBarComponent.setValue(100);
            progressBarComponent.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.TEXT_ONLY);
            if(plugin.paused)
            {
                progressBarComponent.setCenterLabel("Paused");
            }
            else
            {
                progressBarComponent.setCenterLabel("Completed");
            }

        }
        else
        {
            if(!plugin.paused)
            {
                progressBarComponent.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.PERCENTAGE);
            }
            progressBarComponent.setValue(percentDone);
        }


    }



    @Override
    public Dimension render(Graphics2D graphics)
    {

        if ((plugin.killsThisSession == 0) || (fileRW.startKc == 0 && fileRW.endKc == 0) || (!config.displayBossGoalsOverlay()))
        {
            return null;
        }

        iconAndInfoPanel.getChildren().clear();
        this.createPanels();

        graphics.setFont(FontManager.getRunescapeSmallFont());

        panelComponent.getChildren().add(iconAndInfoPanel);
        panelComponent.getChildren().add(progressBarComponent);

        return super.render(graphics);
    }

    // BufferedImage overlayIcon = ImageUtil.resizeImage(itemManager.getImage(ItemID.YOUNGLLEF), 36, 32);
    //
    //        final Image resized = overlayIcon.getScaledInstance(32, 28, Image.SCALE_SMOOTH);
    //        BufferedImage toPutInComponent = ImageUtil.bufferedImageFromImage(resized);


}

