package com.killsperhour;


import net.runelite.api.ItemID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.ui.components.ProgressBar;
import net.runelite.client.util.*;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class KphPanel extends PluginPanel {

    private static final String HTML_LABEL_TEMPLATE = "<html><body style='color:%s'>%s<span style='color:white'>%s</span></body></html>";

    private final JTextField searchField = new JTextField();

    private final JLabel historicalInfoLabel = new JLabel();
    private final JLabel totalTrackedTime = new JLabel();
    private final JLabel totalTrackedKills = new JLabel();
    private final JLabel actualTotalAverageKillTime = new JLabel();
    private final JLabel actualTotalKph = new JLabel();
    private final JLabel overallFastestKill = new JLabel();
    private final JLabel sessionTimeLabel = new JLabel(htmlLabel("Session Time: ","N/A"));
    private final JLabel totalBossKillsLabel = new JLabel(htmlLabel("Kills: ","N/A"));
    private final JLabel averageKillTimeLabel = new JLabel(htmlLabel("Average Kill: ", "N/A"));
    private final JLabel fastestKillTimeLabel = new JLabel(htmlLabel("Fastest Kill: ","N/A"));
    private final JLabel killsPerHourLabel = new JLabel(htmlLabel("KPH: ","N/A"));
    private final JLabel idleTimeLabel = new JLabel(htmlLabel("Idle Time: ","N/A"));
    private final JLabel fetchedBossName = new JLabel("Unknown Boss");
    private final JLabel fetchedTotalTimeActual = new JLabel(htmlLabel("Total Session Time: ",  "N/A"));
    private final JLabel fetchedTotalTimeVirtual = new JLabel(htmlLabel("Total Boss Time: ","N/A"));
    private final JLabel fetchedTotalTrackedKills = new JLabel(htmlLabel("Tracked Kills: ","N/A"));
    private final JLabel fetchedFastestKill = new JLabel(htmlLabel("Fastest Kill: ","N/A"));
    private final JLabel fetchedKillsPerHour = new JLabel(htmlLabel("KPH: ", "N/A"));
    private final JLabel fetchedTotalBossKc = new JLabel(htmlLabel("Total KC: ","N/A"));
    private final JLabel fetchedAverageKillTime = new JLabel(htmlLabel("Average Kill: ","N/A"));
    private final JLabel estimatedTimeSpentBossing = new JLabel(htmlLabel("EST Bossing Time: ", "N/A"));
    private final JLabel lookupHeaderTitle = new JLabel("Boss Info Lookup");
    private final JLabel searchIcon = new JLabel(SEARCH_ICON);
    private final JLabel picLabel = new JLabel();
    private final JLabel fetchedIconLabel = new JLabel();
    private final JLabel startKcLabel = new JLabel("Start KC:");
    private final JLabel endKcLabel = new JLabel("End KC:");
    private final JLabel bossGoalsKphLabel = new JLabel(htmlLabel("KPH: ","N/A"));
    private final JLabel killsDoneLabel = new JLabel(htmlLabel("Kills Done: ","N/A"));
    private final JLabel killsLeftLabel = new JLabel(htmlLabel("Kills Left: ", "N/A"));
    private final JLabel timeToGoalLabel = new JLabel(htmlLabel("TTG: ", "N/A"));
    private final JLabel bossGoalsIconLabel = new JLabel();
                  JLabel currentBossNameLabel = new JLabel("Unknown Boss Name");

    private final JPanel lookupPanelHeaderContents = new JPanel();
    private final JPanel historicalInfoSection = new JPanel(new GridBagLayout());
    private final JPanel buttonIconTab;
    private final JPanel icon;
    private final JPanel fetchedIcon;
    private final JPanel closeAndTrashButtonPanel;
    private final JPanel sidePanel;
    private final JPanel titlePanel;
            final JPanel fetchedInfoPanel;

    private final JPanel lootPanel;

    private final JPanel lootHeaderPanel;

    private final JPanel bossInfoPanel;
    private final JPanel historicalInfoPanel;
    private final JPanel lookupInfoPanelHeader;
    private final JPanel pauseAndResumeButtons;
    private final JPanel sessionEndButton;
    private final JPanel supportButtons;
    private final JPanel bossGoalsInputPanel = new JPanel();
    private final JPanel goalInfoPanel = new JPanel();
    private final JPanel goalInfoPanel2 = new JPanel();
    private final JPanel progressBarPanel = new JPanel(new BorderLayout());
            final JPanel bossGoalsPanel;

    private final JToggleButton dropdownButton = new JToggleButton();
    private final JButton closeButton = new JButton(CLOSE_ICON);
    private final JButton trashButton = new JButton(TRASH_ICON);
    private final JButton searchButton = new JButton();
                  JButton pauseResumeButton = new JButton();
                  JButton switchModeButton = new JButton();

    final SpinnerNumberModel startKcModel = new SpinnerNumberModel(0, 0, 10000000, 1);
    final SpinnerNumberModel endKcModel = new SpinnerNumberModel(0, 0, 10000000, 5); //change step size to 5

    private final JSpinner startKcSpinner = new JSpinner(startKcModel);
    private final JSpinner endKcSpinner = new JSpinner(endKcModel);

    final ProgressBar progressBar = new ProgressBar();

    private static final ImageIcon TRASH_ICON;
    private static final ImageIcon TRASH_ICON_HOVER;
    private static final ImageIcon CLOSE_ICON;
    private static final ImageIcon CLOSE_ICON_HOVER;
    private static final ImageIcon SEARCH_ICON;
    private static final ImageIcon SEARCH_ICON_HOVER;
    private static final ImageIcon DROPDOWN_ICON;
    private static final ImageIcon DROPDOWN_HOVER;
    private static final ImageIcon DROPDOWN_FLIPPED_ICON;
    private static final ImageIcon DROPDOWN_FLIPPED_HOVER;
    private static final ImageIcon DISCORD_ICON;
    private static final ImageIcon DISCORD_HOVER;
    private static final ImageIcon GITHUB_ICON;
    private static final ImageIcon GITHUB_HOVER;
    private static final ImageIcon VISIBLE_ICON;
    private static final ImageIcon INVISABLE_ICON;

    private AsyncBufferedImage fetchedBossSprite;

    private final KphConfig config;

    private final KphPlugin plugin;

    private final FileReadWriter fileRW;

    private KphBossGoalsOverlay goalsOverlay;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ClientThread clientThread;


    @Inject
    KphPanel(KphPlugin plugin, KphConfig config, FileReadWriter fileRW)
    {
        this.fetchedIcon = new JPanel();
        this.fetchedInfoPanel = new JPanel();
        this.closeAndTrashButtonPanel = new JPanel();
        this.sessionEndButton = new JPanel();
        this.pauseAndResumeButtons = new JPanel();
        this.supportButtons = new JPanel();
        this.sidePanel = new JPanel();
        this.titlePanel = new JPanel();
        this.bossInfoPanel = new JPanel();
        this.historicalInfoPanel = new JPanel();
        this.lookupInfoPanelHeader = new JPanel();
        this.buttonIconTab = new JPanel();
        this.lootPanel = new JPanel();
        this.lootHeaderPanel = new JPanel();
        this.bossGoalsPanel = new JPanel();
        this.icon = new JPanel();
        this.plugin = plugin;
        this.config = config;
        this.fileRW = fileRW;
    }

    void sidePanelInitializer()
    {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.sidePanel.setLayout(new BoxLayout(this.sidePanel,BoxLayout.Y_AXIS));
        this.sidePanel.add(this.buildTitlePanel());
        this.sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        this.sidePanel.add(this.buildBossInfoPanel());
        this.sidePanel.add(this.buildBossGoalsPanel());
        this.sidePanel.add(this.buildButtonIconTab());
        this.sidePanel.add(this.buildGridTest());
        this.sidePanel.add(this.buildLootPanel());
        this.sidePanel.add(this.buildPauseAndResumebuttons());
        this.sidePanel.add(this.buildSessionEndButton());
        this.sidePanel.add(this.buildSupportbuttons());

        //ensure to build all the panels on the initial load, do not build them everytime i add or remove.
        buildFetchedInfoPanel();
        buildlookupInfoPanelHeader();
        buildHistoricalInfoPanel();

        if(!config.displayBossGoalsPanel())
        {
            bossGoalsPanel.setVisible(false);
        }

        this.add(sidePanel, "North");
    }


    JToggleButton lootHeaderButtonPanel = new JToggleButton();
    JToggleButton hideItemButton = new JToggleButton();
    JLabel gpPerHourLabel = new JLabel(htmlLabel("Gp/Hr: ","N/A"));
    JLabel gpPerKill = new JLabel(htmlLabel("Gp/Kill: ","N/A"));
    JLabel totalGpLabel = new JLabel(htmlLabel("Total Gp: ","N/A"));
    JLabel killsTrackedLabel = new JLabel(htmlLabel("Kills: ","N/A"));
    JPanel masterPanel = new JPanel();


    private JPanel buildGridTest()
    {

        masterPanel.setLayout(new BorderLayout());

        lootHeaderPanel.setLayout(new BorderLayout());
        lootHeaderPanel.setBorder(new EmptyBorder(0,0,0,0));
        lootHeaderPanel.setBorder(new MatteBorder(1, 1, 1, 1, new Color(57, 57,57)));


        JPanel lootHeaderSubContainer = new JPanel();
        JPanel bottomInfo = new JPanel();
        JPanel topInfo = new JPanel();


        lootHeaderButtonPanel.setLayout(new GridLayout(2,0,0,0));
        lootHeaderSubContainer.setLayout(new BorderLayout());
        lootHeaderSubContainer.setBorder(new MatteBorder(1, 1, 1, 1, new Color(57, 57,57)));
        bottomInfo.setLayout(new GridLayout(0,2,0,0));
        topInfo.setLayout(new BorderLayout());


        lootHeaderSubContainer.setBorder(new EmptyBorder(5,0,0,0));

        lootHeaderButtonPanel.setPreferredSize(new Dimension(200,35));

        lootHeaderButtonPanel.setBorder(new EmptyBorder(4,5,0,5));

        hideItemButton.setIcon(INVISABLE_ICON);
        hideItemButton.setSelectedIcon(VISIBLE_ICON);



        gpPerHourLabel.setFont(FontManager.getRunescapeSmallFont());
        gpPerKill.setFont(FontManager.getRunescapeSmallFont());
        totalGpLabel.setFont(FontManager.getRunescapeSmallFont());
        killsTrackedLabel.setFont(FontManager.getRunescapeSmallFont());


        SwingUtil.removeButtonDecorations(hideItemButton);
        SwingUtil.removeButtonDecorations(lootHeaderButtonPanel);
        lootHeaderButtonPanel.setRolloverEnabled(false);

        hideItemButton.setPreferredSize(new Dimension(20,18));

        hideItemButton.addActionListener(e -> clientThread.invoke(() -> updateLootGrid(lootDisplayMap())));

        lootHeaderButtonPanel.addActionListener(e -> collapseLoot());

        gpPerKill.setForeground(Color.WHITE);

        topInfo.setBorder(new EmptyBorder(0,0,0,0));

        topInfo.add(gpPerKill,"West");
        topInfo.add(hideItemButton,"East");


        killsTrackedLabel.setBorder(new EmptyBorder(0,48,0,0));

        topInfo.add(killsTrackedLabel,"Center");
        bottomInfo.add(gpPerHourLabel);
        bottomInfo.add(totalGpLabel);
        topInfo.setBackground(new Color(30, 30, 30));
        bottomInfo.setBackground(new Color(30, 30, 30));

        lootHeaderButtonPanel.add(topInfo,"North");
        lootHeaderButtonPanel.add(bottomInfo,"South");
        lootHeaderButtonPanel.setBackground(new Color(30, 30, 30));
        lootHeaderPanel.setBackground(new Color(30, 30, 30));
        lootHeaderPanel.add(lootHeaderButtonPanel);

        masterPanel.add(lootHeaderSubContainer,"North");
        masterPanel.add(lootHeaderPanel,"South");

        return masterPanel;

    }



    public Map<Integer,Integer> lootDisplayMap()
    {
        if(plugin.getPanel().fetchedInfoPanel.isShowing())
        {
           return fileRW.fetchedAllItemDrops;
        }
        else if (fileRW.cachedItemDrops != null && plugin.sessionNpc == null)
        {
            return fileRW.cachedItemDrops;
        }
        else if((!plugin.getPanel().fetchedInfoPanel.isShowing()) && (config.lootDisplay() == KphConfig.LootDisplay.ALL_TIME) && plugin.sessionNpc != null)
        {
            return fileRW.allItemDrops;
        }
        else
        {
            return fileRW.sessionItemDrops;
        }

    }



    static String doubleFormatNumber(double num)
    {
        DecimalFormat twoP = new DecimalFormat("#.##");
        DecimalFormat oneP = new DecimalFormat("#.#");
        if (num >= 1000000000) { num = num / 1000000000; return twoP.format(num) + "B"; } //1B
        if (num >= 1000000) { num = num / 1000000; return twoP.format(num) + "M"; } //1M
        if (num >= 1000) { num = num / 1000; return oneP.format(num) + "K"; } //1K
        else { return oneP.format(num); }
    }

    public double convertToGpPerHour(int kills,double Kph)
    {
      return  ((fileRW.totalGp / kills) * Kph);
    }

    public double convertToGpPerKill(int kills)
    {
        return  (fileRW.totalGp / kills);
    }


    public void updateLootHeaderInfo(int kills, double Kph)
    {
        FontMetrics fontMetrics = getGraphics().getFontMetrics(FontManager.getRunescapeSmallFont());
        totalGpLabel.setText(htmlLabel("Total Gp: ",doubleFormatNumber(fileRW.totalGp)));
        gpPerHourLabel.setText(htmlLabel("GP/Hr: ",doubleFormatNumber(convertToGpPerHour(kills,Kph))));
        gpPerKill.setText(htmlLabel("GP/Kill: ",doubleFormatNumber(convertToGpPerKill(kills))));
        if (fileRW.totalGp == 0) { kills = 0; }
        killsTrackedLabel.setText(htmlLabel("Kills: ", String.valueOf(kills)));
        killsTrackedLabel.setBorder(new EmptyBorder(0,  535 - fontMetrics.stringWidth(gpPerKill.getText())  ,0,0));
    }


    public void collapseLoot()
    {
        lootPanel.setVisible(!lootHeaderButtonPanel.isSelected());
    }




    private static final int ITEMS_PER_ROW = 5;
    private static final Dimension ITEM_SIZE = new Dimension(40, 40);

    public void updateLootGrid(Map<Integer,Integer> lootMap)
    {
        for (Map.Entry<Integer, Integer> entry : lootMap.entrySet())
        {
            double itemPrice = itemManager.getItemPrice(entry.getKey());
            itemPrice = itemPrice * entry.getValue();
            fileRW.itemAndTotalPrice.put(entry.getKey(),itemPrice);
        }
        SwingUtilities.invokeLater(() -> lootGrid(lootMap));
    }

    JPanel containerPanel = new JPanel();


    public void lootGrid(Map<Integer,Integer> lootMap)
    {
        fileRW.loadIgnoredList(provideBossName());
        HashMap<Integer, Integer> sorted = new HashMap<>();
        if (!fileRW.itemAndTotalPrice.isEmpty())
        {
            sorted = fileRW.sortByValue(fileRW.itemAndTotalPrice,lootMap);
        }

        JPanel containerCurrent = new JPanel();

       // HashMap<Integer, Integer> sorted = fileRW.sortByValue(fileRW.itemAndTotalPrice,lootMap);

        int totalItems;

        if(hideItemButton.isSelected() || fileRW.ignored == null)
        {
            totalItems = sorted.keySet().size();
        }
        else
        {
            totalItems = sorted.keySet().size() - fileRW.ignored.size();
        }


        containerPanel.setBorder(new EmptyBorder(2, 2, 5, 2));
        containerCurrent.setBorder(new EmptyBorder(2, 2, 5, 2));


        // Calculates how many rows need to be display to fit all items
        final int rowSize = ((totalItems % ITEMS_PER_ROW == 0) ? 0 : 1) + totalItems / ITEMS_PER_ROW;
        containerPanel.setLayout(new GridLayout(rowSize, ITEMS_PER_ROW, 1, 1));
        containerCurrent.setLayout(new GridLayout(rowSize, ITEMS_PER_ROW, 1, 1));


       // Create stacked items from the item list, calculates total price and then displays all the items in the UI.
        for (Integer key : sorted.keySet())
        {
            final JPanel slot = new JPanel();
            slot.setLayout(new GridLayout(1, 1, 0, 0));
            slot.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            slot.setPreferredSize(ITEM_SIZE);

            final JLabel itemLabel = new JLabel();

            clientThread.invoke(() -> itemLabel.setToolTipText(buildToolTip(key,lootMap)));
            itemLabel.setVerticalAlignment(SwingConstants.CENTER);
            itemLabel.setHorizontalAlignment(SwingConstants.CENTER);

            AsyncBufferedImage itemImage = itemManager.getImage( key,  sorted.get(key),  sorted.get(key) > 1);
            itemImage.addTo(itemLabel);


            final JPopupMenu popupMenu = new JPopupMenu();
            itemLabel.setComponentPopupMenu(popupMenu);

            final JMenuItem toggle = new JMenuItem("Toggle item");
            toggle.addActionListener(e ->
            {
                fileRW.loadIgnoredList(provideBossName());
                if(fileRW.ignored.contains(key))
                {
                    fileRW.ignored.remove(key);
                }
                else
                {
                    fileRW.ignored.add(key);
                }

                fileRW.writeIgnoredListToFile(provideBossName());
                clientThread.invoke(() -> updateLootGrid(lootMap));
            });


            fileRW.loadIgnoredList(provideBossName());
            if(!fileRW.ignored.contains(key) || hideItemButton.isSelected())
            {
                popupMenu.add(toggle);

                if(fileRW.ignored.contains(key))
                {
                    Runnable addTransparency = () ->
                    {
                        BufferedImage transparentImage = ImageUtil.alphaOffset(itemImage, .3f);
                        itemLabel.setIcon(new ImageIcon(transparentImage));
                    };
                    itemImage.onLoaded(addTransparency);
                    addTransparency.run();
                }
                slot.add(itemLabel);
                containerCurrent.add(slot);

            }

        }



        if(totalItems < 5 || totalItems % 5 != 0)
        {
            int extraBoxes;
            if(totalItems % 5 != 0 && totalItems >= 5)
            {
                int i = totalItems;
                while (i % 5 != 0)
                {
                    i++;
                }
                extraBoxes = i - totalItems;
            }
            else
            {
                extraBoxes = 5 - totalItems;
            }

            for (int i = 0; i < extraBoxes; i++)
            {
                final JPanel slot = new JPanel();
                slot.setLayout(new GridLayout(1, 1, 0, 0));
                slot.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                slot.setPreferredSize(ITEM_SIZE);

                containerCurrent.add(slot);
            }
        }

        lootPanel.remove(containerPanel);
        this.containerPanel = containerCurrent;
        lootPanel.add(containerPanel);

        fileRW.getTotalPrice();






        //clean this up. make sure it does not trigger twice causes issues

        if(plugin.getPanel().fetchedInfoPanel.isShowing())
        {
            updateLootHeaderInfo(fileRW.lootKillsTracked,fileRW.fetchedKillsPerHour);
        }

        if((!plugin.getPanel().fetchedInfoPanel.isShowing()) && (config.lootDisplay() == KphConfig.LootDisplay.ALL_TIME))
        {
            fileRW.setFilename(plugin.currentBoss);
            fileRW.fetchLookupInfo();
            updateLootHeaderInfo(fileRW.lootKillsTracked,fileRW.fetchedKillsPerHour);
        }

        if((!plugin.getPanel().fetchedInfoPanel.isShowing()) && (config.lootDisplay() == KphConfig.LootDisplay.SESSION))
        {
            updateLootHeaderInfo(plugin.killsThisSession,Double.parseDouble(plugin.formatKPH()));
        }

        if (!plugin.getPanel().fetchedInfoPanel.isShowing() && plugin.sessionNpc == null && plugin.cacheHasInfo)
        {
            updateLootHeaderInfo(plugin.cachedSessionKills,Double.parseDouble(plugin.cachedKPH));
        }





        lootPanel.revalidate();
        lootPanel.repaint();
    }


    public String provideBossName()
    {
        if(plugin.getPanel().fetchedInfoPanel.isShowing())
        {
            return searchInput;
        }
        else
        {
            return plugin.currentBoss;
        }
    }

    private String buildToolTip(int key,Map<Integer,Integer> lootMap)
    {
        final String name = itemManager.getItemComposition(key).getName();
        final int quantity = lootMap.get(key);
        final long price = itemManager.getItemPrice(key);

        return "<html>" + name + " x " + QuantityFormatter.formatNumber(quantity)
                + "<br/>Price: " + QuantityFormatter.quantityToStackSize(price)
                + "<br/>Total: " + QuantityFormatter.quantityToStackSize(quantity * price) + "</html>";
    }




    private JPanel buildLootPanel()
    {
        lootPanel.setLayout(new BorderLayout());
        lootPanel.setBorder(new EmptyBorder(0, 0, 8, 0));
        lootPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(37, 125, 141)));
        return lootPanel;
    }







    int killsDone;
    int totalKillsToGet;
    int killsLeft;
    double TTG;
    double percentDone;

    //this empty label is needed to make sure the txt is centered on the progress bar *don't ask me how or why it works, its magic*
    //private final JLabel magicCenteringLabel = new JLabel();

    public void updateBossGoalsPanel()
    {
        if(plugin.sessionNpc == null)
        {
            return;
        }

        if(fileRW.startKc == 0 && fileRW.endKc == 0)
        {
            bossGoalsKphLabel.setText(htmlLabel("KPH: ","N/A"));
            killsDoneLabel.setText(htmlLabel("Kills Done: ","N/A"));
            killsLeftLabel.setText(htmlLabel("Kills Left: " ,"N/A"));
            timeToGoalLabel.setText(htmlLabel("TTG: ", "N/A"));
            KphBossInfo kphBossInfo = KphBossInfo.find(plugin.sessionNpc);
            itemManager.getImage(kphBossInfo.getIcon()).addTo(bossGoalsIconLabel);
            bossGoalsIconLabel.setVisible(true);
            goalInfoPanel.setBorder(new EmptyBorder(5, 0, 0, 17));
            progressBar.setLeftLabel("");
            progressBar.setRightLabel("");
            progressBar.setCenterLabel("Set a goal");
            progressBar.setValue(0);
            progressBar.setDimmed(false);
            return;

        }

        DecimalFormat df = new DecimalFormat("#.#");

        killsDone = plugin.killCount - fileRW.startKc;

        totalKillsToGet = fileRW.endKc - fileRW.startKc;

        killsLeft = totalKillsToGet - killsDone;

        TTG = killsLeft / plugin.killsPerHour;

        percentDone = 100 * ((double) killsDone / (double) totalKillsToGet);

        if(killsLeft <= 0)
        {
            bossGoalsKphLabel.setText(htmlLabel("KPH: ",plugin.formatKPH()));
            killsDoneLabel.setText(htmlLabel("Kills Done: ",String.valueOf(totalKillsToGet)));
            killsLeftLabel.setText(htmlLabel("Kills Left: ","0"));
            timeToGoalLabel.setText(htmlLabel("TTG: ","00:00:00"));
            progressBar.setCenterLabel("Completed");
            progressBar.setValue(100);

        }
        else
        {
            bossGoalsKphLabel.setText(htmlLabel("KPH: ",plugin.formatKPH()));
            killsDoneLabel.setText(htmlLabel("Kills Done: ",String.valueOf(killsDone)));
            killsLeftLabel.setText(htmlLabel("Kills Left: ",String.valueOf(killsLeft)));
            timeToGoalLabel.setText(htmlLabel("TTG: ",plugin.timeConverter((int)(TTG * 3600))));

            percentDone = Double.parseDouble(df.format(percentDone));
            progressBar.setCenterLabel(percentDone + "%");
            progressBar.setValue((int)percentDone);
        }


        if(config.displayRelativeKills())
        {
            progressBar.setLeftLabel(String.valueOf(0));
            progressBar.setRightLabel(String.valueOf(totalKillsToGet));
        }
        else
        {
            progressBar.setLeftLabel(String.valueOf(fileRW.startKc));
            progressBar.setRightLabel(String.valueOf(fileRW.endKc));
        }

        int length;

        FontMetrics fontMetrics = getGraphics().getFontMetrics(FontManager.getRunescapeSmallFont());
        boolean killsDoneIsLonger = (fontMetrics.stringWidth(killsLeftLabel.getText()) < fontMetrics.stringWidth(killsDoneLabel.getText()));

        if(killsDoneIsLonger){length = fontMetrics.stringWidth(killsDoneLabel.getText());}
        else {length = fontMetrics.stringWidth(killsLeftLabel.getText());}

        int offset;
        offset = 521 - length;
        goalInfoPanel.setBorder(new EmptyBorder(5, 0, 0, offset));


        KphBossInfo kphBossInfo = KphBossInfo.find(plugin.sessionNpc);
        itemManager.getImage(kphBossInfo.getIcon()).addTo(bossGoalsIconLabel);

        bossGoalsIconLabel.setVisible(true);
        progressBar.setForeground(new Color(91, 154, 47));
        progressBar.setDimmed(false);

        if(plugin.paused)
        {
            progressBar.setCenterLabel("Paused");
            progressBar.setForeground(new Color(167, 125, 38));
        }

        progressBar.repaint();

    }


    private JPanel buildBossGoalsPanel()
    {
        bossGoalsPanel.setLayout(new BorderLayout());

        bossGoalsPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        bossGoalsPanel.setBorder(new MatteBorder(1, 1, 1, 1, new Color(49, 49, 49)));


        progressBarPanel.setBorder(new EmptyBorder(5, 5, 7, 5));


        progressBar.setBackground(new Color(61, 56, 49));
        progressBar.setForeground(new Color(91, 154, 47));


        progressBar.setMaximumValue(100);
        progressBar.setCenterLabel(percentDone + "%");
        progressBar.setValue((int)percentDone);


        progressBarPanel.add(progressBar);
        progressBarPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JMenuItem setBossGoalMenuItem = new JMenuItem("Set Boss Goal");
        setBossGoalMenuItem.addActionListener(e -> bossGoalsOptionPane());

        JMenuItem resetBossGoalMenuItem = new JMenuItem("Reset Boss Goal");
        resetBossGoalMenuItem.addActionListener(e -> fileRW.resetBossGoal());

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));

        popupMenu.add(setBossGoalMenuItem);
        popupMenu.add(resetBossGoalMenuItem);


        bossGoalsPanel.setComponentPopupMenu(popupMenu);



        JPanel iconPanel = new JPanel(new GridBagLayout());


        iconPanel.setLayout(new GridLayout(0, 1, 0, 0));
        //this controls the offset of the current boss name, useful for alinging the icon
        iconPanel.setBorder(new EmptyBorder(5, 5, 0, 0));



        bossGoalsKphLabel.setFont(FontManager.getRunescapeSmallFont());
        killsDoneLabel.setFont(FontManager.getRunescapeSmallFont());
        timeToGoalLabel.setFont(FontManager.getRunescapeSmallFont());
        killsLeftLabel.setFont(FontManager.getRunescapeSmallFont());



        goalInfoPanel.setLayout(new GridLayout(2, 0, 0, 0));
        goalInfoPanel.setBorder(new EmptyBorder(5, 0, 0, 17));

        goalInfoPanel2.setLayout(new GridLayout(2, 0, 0, 0));
        goalInfoPanel2.setBorder(new EmptyBorder(5, 3, 0, 3));

        //placeholder image used to keep spaceing consistnat
        bossGoalsIconLabel.setIcon(new ImageIcon(itemManager.getImage(ItemID.YOUNGLLEF)));
        bossGoalsIconLabel.setVisible(false);

        //sets the continer panel to opaque or not, false = transparent, this will only affect the assingned panel. it will not affect any other content or panel within said panel.
        iconPanel.setOpaque(false);
        goalInfoPanel.setOpaque(false);
        goalInfoPanel2.setOpaque(false);
        progressBarPanel.setOpaque(false);

        //adds the lables to the respective panel in the order they are added
        iconPanel.add(bossGoalsIconLabel);
        goalInfoPanel2.add(bossGoalsKphLabel);
        goalInfoPanel.add(killsDoneLabel);
        goalInfoPanel2.add(timeToGoalLabel);
        goalInfoPanel.add(killsLeftLabel);

        bossGoalsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        bossGoalsPanel.add(goalInfoPanel2);
        bossGoalsPanel.add(goalInfoPanel,"East");
        bossGoalsPanel.add(iconPanel, "West");
        bossGoalsPanel.add(progressBarPanel,"South");

        return bossGoalsPanel;
    }






    private JPanel buildlookupInfoPanelHeader()
    {


                //change vgap to move title
        lookupPanelHeaderContents.setLayout(new GridLayout(0, 1, 0, 5));
        lookupPanelHeaderContents.setBorder(new EmptyBorder(0, 0, 10, 20));

        searchField.setBackground(new Color(57, 57,57));
        searchField.setBorder(new MatteBorder(1, 1, 1, 1, new Color(30, 30, 30)));

        searchField.setMaximumSize(new Dimension(167,20));
        searchField.setMinimumSize(new Dimension(167,20));
        searchField.setPreferredSize(new Dimension(167,20));


        searchField.addActionListener(e -> fetchLookupInfo());


        lookupInfoPanelHeader.setLayout(new BorderLayout());
        lookupInfoPanelHeader.setBorder(new EmptyBorder(5, 0, 4, 0));
        lookupInfoPanelHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(37, 125, 141)));


        searchIcon.setBorder(new EmptyBorder(13, 10, 0, 0));

        //adds the lables to the respective panel in the order they are added
        lookupHeaderTitle.setFont(FontManager.getRunescapeBoldFont());
        lookupHeaderTitle.setBorder(new EmptyBorder(0, 20, 0, 0));

        lookupPanelHeaderContents.add(lookupHeaderTitle);
        lookupPanelHeaderContents.add(searchField);

        lookupInfoPanelHeader.add(searchIcon,"West");
        lookupInfoPanelHeader.add(lookupPanelHeaderContents,"East");
        return lookupInfoPanelHeader;
    }




    String searchInput;

    public void fetchLookupInfo()
    {
        searchInput = searchField.getText();

        if(KphBossInfo.bossByWords.containsKey(searchInput.toLowerCase()))
        {
           searchInput = KphBossInfo.bossByWords.get(searchInput.toLowerCase());
        }

        fileRW.setFilename(searchInput);
        fileRW.fetchLookupInfo();

        for (KphBossInfo bossIconFinder : KphBossInfo.values())
        {
            if (searchInput.toLowerCase().equals(bossIconFinder.getName().toLowerCase()))
            {
                KphBossInfo kphBossInfo = KphBossInfo.find(bossIconFinder.getName());
                fetchedBossSprite = itemManager.getImage(kphBossInfo.getIcon());
                fetchedBossName.setText(kphBossInfo.getName());
                fetchedBossSprite.addTo(fetchedIconLabel);
                fetchedIconLabel.setVisible(true);

                break;
            }
            else
            {
                fetchedBossName.setText("Unknown Boss");
                fetchedIconLabel.setVisible(false);
            }

        }

        FontMetrics fontMetrics = getGraphics().getFontMetrics(FontManager.getRunescapeBoldFont());
        int offset = 180 - (fontMetrics.stringWidth(fetchedBossName.getText()) + 10);
                                                        //if i chan
        fetchedIcon.setBorder(new EmptyBorder(0, 0, 0,offset));

        updateLookupInfo();


    }

    public void updateLookupInfo()
    {
        if(fileRW.fetchedFile == null || !fileRW.fetchedFile.exists())
        {
            setLookupInfoToNull();
            getFetchedLoot();
            return;
        }

        fileRW.fetchedStatConverter();
        fetchedFastestKill.setText(htmlLabel("Fastest Kill: ",plugin.timeConverter(fileRW.fetchedFastestKill)));
        fetchedTotalTrackedKills.setText(htmlLabel("Tracked Kills: ",String.valueOf(fileRW.fetchedTotalTrackedKills)));
        fetchedTotalTimeActual.setText(htmlLabel("Total Session Time: ",plugin.timeConverter(fileRW.fetchedTotalTimeActual)));
        fetchedTotalTimeVirtual.setText(htmlLabel("Total Boss Time: ", plugin.timeConverter(fileRW.fetchedTotalTimeVirtual)));
        fetchedKillsPerHour.setText(htmlLabel("KPH: ",String.valueOf(fileRW.fetchedKillsPerHour)));
        fetchedTotalBossKc.setText(htmlLabel("Total KC: ",String.valueOf(fileRW.fetchedTotalBossKc)));
        fetchedAverageKillTime.setText(htmlLabel("Average Kill: ", plugin.timeConverter(fileRW.fetchedAverageKillTime)));
        estimatedTimeSpentBossing.setText(htmlLabel("EST Bossing Time: ", plugin.timeConverter(fileRW.estimatedTimeSpentBossing)));

        getFetchedLoot();

    }


    private Map<Integer,Integer> currentLootTable = new HashMap<>();

    public void getFetchedLoot()
    {
        fileRW.lootDirectory = new File(fileRW.file, "boss-loot");
        fileRW.subDirectory = new File(fileRW.lootDirectory, searchInput + ".json");
        fileRW.loadFetchedDropsFromMap();
       // fileRW.loadIgnoredList(searchInput);
        if(fileRW.fetchedFile == null || !fileRW.fetchedFile.exists())
        {
            fileRW.fetchedAllItemDrops = new HashMap<Integer, Integer>();
            fileRW.ignored = new ArrayList<Integer>();
        }
        fileRW.itemAndTotalPrice = new HashMap<Integer, Double>();

        fileRW.loadIgnoredList(provideBossName());

        clientThread.invoke(() -> updateLootGrid(lootDisplayMap()));

    }


    public void setLookupInfoToNull()
    {
        fetchedFastestKill.setText(htmlLabel("Fastest Kill: ","N/A"));
        fetchedTotalTrackedKills.setText(htmlLabel("Tracked Kills: ", "N/A"));
        fetchedTotalTimeActual.setText(htmlLabel("Total Session Time: ", "N/A"));
        fetchedTotalTimeVirtual.setText(htmlLabel("Total Boss Time: ", "N/A"));
        fetchedKillsPerHour.setText(htmlLabel("KPH: ", "N/A"));
        fetchedTotalBossKc.setText(htmlLabel("Total KC: ", "N/A"));
        fetchedAverageKillTime.setText(htmlLabel("Average Kill: ", "N/A"));
        estimatedTimeSpentBossing.setText(htmlLabel("EST Bossing Time: ", "N/A"));
    }

    

    private JPanel buildFetchedInfoPanel()
    {
        fetchedInfoPanel.setLayout(new BorderLayout());
        fetchedInfoPanel.setBorder(new EmptyBorder(0, 0, 0, 10));

        JPanel fetchedInfoSection = new JPanel();
        fetchedInfoSection.setLayout(new GridLayout(9, 0, 0, 10));
        fetchedInfoSection.setBorder(new EmptyBorder(15, 5, 3, 0));

        closeButton.setMaximumSize(new Dimension(30,25));
        closeButton.setPreferredSize(new Dimension(30,25));
        closeButton.setMinimumSize(new Dimension(30,25));
        closeButton.setBorder(new EmptyBorder(0, 0, 0, 7));
        closeButton.setRolloverIcon(CLOSE_ICON_HOVER);
        closeButton.setToolTipText("Exit to Main Panel");
        closeButton.addActionListener(e -> closeBossLookupPanels());
        SwingUtil.removeButtonDecorations(closeButton);

        closeAndTrashButtonPanel.setLayout(new BorderLayout());
        closeAndTrashButtonPanel.add(closeButton,"East");


        trashButton.setMaximumSize(new Dimension(30,25));
        trashButton.setPreferredSize(new Dimension(30,25));
        trashButton.setMinimumSize(new Dimension(30,25));
        trashButton.setBorder(new EmptyBorder(0, 0, 2, 0));
        trashButton.setRolloverIcon(TRASH_ICON_HOVER);
        trashButton.setToolTipText("Delete Boss Info");

        trashButton.addActionListener(e -> deleteFile());

        SwingUtil.removeButtonDecorations(trashButton);

        closeAndTrashButtonPanel.add(trashButton,"West");


        //adds the lables to the respective panel in the order they are added
        fetchedBossName.setFont(FontManager.getRunescapeBoldFont());
        fetchedBossName.setForeground(new Color(219, 219, 219));


        fetchedIcon.add(fetchedIconLabel);
        fetchedInfoSection.add(fetchedBossName);
        fetchedInfoSection.add(fetchedKillsPerHour);
        fetchedInfoSection.add(fetchedTotalTrackedKills);
        fetchedInfoSection.add(fetchedTotalBossKc);
        fetchedInfoSection.add(fetchedAverageKillTime);
        fetchedInfoSection.add(fetchedFastestKill);
        fetchedInfoSection.add(fetchedTotalTimeVirtual);
        fetchedInfoSection.add(fetchedTotalTimeActual);
        fetchedInfoSection.add(estimatedTimeSpentBossing);


        fetchedInfoPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(37, 125, 141)));

        fetchedIcon.setOpaque(false);
        fetchedInfoSection.setOpaque(false);

        fetchedInfoPanel.add(closeAndTrashButtonPanel,"South");
        fetchedInfoPanel.add(fetchedIcon,"East");
        fetchedInfoPanel.add(fetchedInfoSection, "West");

        return fetchedInfoPanel;
    }



    public static BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight)
    {
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = scaledImage.createGraphics();
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaledImage;
    }


    int startKC;
    int endKC;

    public void bossGoalsOptionPane()
    {

        fileRW.getCurrentBossKCGoal();

        startKcSpinner.setBounds(0,0,50,20);
        startKcSpinner.setMaximumSize(new Dimension(100,20));
        startKcSpinner.setPreferredSize(new Dimension(100,20));
        startKcSpinner.setMinimumSize(new Dimension(100,20));

        endKcSpinner.setMaximumSize(new Dimension(100,20));
        endKcSpinner.setPreferredSize(new Dimension(100,20));
        endKcSpinner.setMinimumSize(new Dimension(100,20));

        //endKcSpinner.setToolTipText("+/- 5");
        endKcSpinner.addChangeListener(e -> getRelativeGoal());

        bossGoalsInputPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        bossGoalsInputPanel.setLayout(new GridLayout(0,2,0,5));

        bossGoalsInputPanel.setMaximumSize(new Dimension(100,20));
        bossGoalsInputPanel.setPreferredSize(new Dimension(100,20));
        bossGoalsInputPanel.setMinimumSize(new Dimension(100,20));

        startKcLabel.setMaximumSize(new Dimension(120,20));
        startKcLabel.setPreferredSize(new Dimension(120,20));
        startKcLabel.setMinimumSize(new Dimension(120,20));

        endKcLabel.setMaximumSize(new Dimension(120,20));
        endKcLabel.setPreferredSize(new Dimension(120,20));
        endKcLabel.setMinimumSize(new Dimension(120,20));

        endKcLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
        startKcLabel.setBorder(new EmptyBorder(0, 0, 0, 20));

        bossGoalsInputPanel.add(startKcLabel);
        bossGoalsInputPanel.add(startKcSpinner);
        bossGoalsInputPanel.add(endKcLabel);
        bossGoalsInputPanel.add(endKcSpinner);


        KphBossInfo kphBossInfo =  KphBossInfo.find(plugin.sessionNpc);
        AsyncBufferedImage bossSprite = itemManager.getImage(kphBossInfo.getIcon());
        ImageIcon imageIcon = new ImageIcon(resizeImage(bossSprite,50,44));

        UIManager.put("OptionPane.minimumSize",new Dimension(250,100));

        int option = JOptionPane.showOptionDialog(null, bossGoalsInputPanel, "Set Boss KC Goal", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,imageIcon, null, null);

        if (option == JOptionPane.OK_OPTION)
        {

            startKC = (int) startKcSpinner.getValue();
            endKC = (int) endKcSpinner.getValue();
            fileRW.updateBossKCGoal();
            updateBossGoalsPanel();
        }

    }

    public void getRelativeGoal()
    {
        endKcSpinner.setToolTipText("+" + ((int) endKcSpinner.getValue() - (int) startKcSpinner.getValue()));
    }

    public void deleteFile()
    {
        if(fileRW.fetchedFile.exists())
        {
            int confirm = JOptionPane.showConfirmDialog(
                    KphPanel.this,
                    "Are you sure you want to permanently delete these files",
                    "Warning", JOptionPane.OK_CANCEL_OPTION);

            if (confirm == 0)
            {
               //SwingUtilities.invokeLater(this::deleteTest);
               deleteTest();
            }
        }
        else
        {

        }

    }


    public void deleteTest()
    {

        try
        {
            Files.delete(fileRW.lookupPath);
            fileRW.fetchedAllItemDrops = new HashMap<Integer, Integer>();
            fileRW.ignored = new ArrayList<Integer>();

                try
                {
                    Writer writer = new FileWriter(fileRW.subDirectory);
                    writer.write("[]");
                    writer.close();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }


            try
            {
                Writer writer = new FileWriter(fileRW.ignoreDirectory);
                writer.write("[]");
                writer.close();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
        catch (Exception Io)
        {
            Io.printStackTrace();

        }

    }



    


    public void closeBossLookupPanels()
    {
        sidePanel.remove(fetchedInfoPanel);
        sidePanel.remove(lookupInfoPanelHeader);
        sidePanel.add(bossInfoPanel);
        sidePanel.add(buttonIconTab);
        sidePanel.add(bossGoalsPanel);
        sidePanel.setComponentZOrder(bossInfoPanel,2);
        sidePanel.setComponentZOrder(bossGoalsPanel,3);
        sidePanel.setComponentZOrder(buttonIconTab,4);
        sidePanel.setComponentZOrder(lootHeaderPanel,5);
        sidePanel.setComponentZOrder(lootPanel,6);
        sidePanel.revalidate();

        fileRW.loadIgnoredList(plugin.currentBoss);


        clientThread.invoke(() -> updateLootGrid(lootDisplayMap()));
    }

    public void openBossLookupPanels()
    {
        sidePanel.remove(buttonIconTab);
        sidePanel.remove(bossInfoPanel);
        sidePanel.remove(historicalInfoPanel);
        sidePanel.remove(bossGoalsPanel);
        sidePanel.add(lookupInfoPanelHeader);
        sidePanel.add(fetchedInfoPanel);
        sidePanel.setComponentZOrder(lookupInfoPanelHeader,2);
        sidePanel.setComponentZOrder(fetchedInfoPanel,3);
        sidePanel.setComponentZOrder(lootHeaderPanel,4);
        sidePanel.setComponentZOrder(lootPanel,5);
        sidePanel.setComponentZOrder(pauseAndResumeButtons,6);
        sidePanel.setComponentZOrder(sessionEndButton,7);
        dropdownButton.setSelected(false);
        sidePanel.revalidate();

        fetchLookupInfo();
    }


    public void openCurrentHistoricalData()
    {
        sidePanel.add(historicalInfoPanel);
        sidePanel.setComponentZOrder(buttonIconTab,4);
        sidePanel.setComponentZOrder(historicalInfoPanel,5);
        sidePanel.revalidate();
        fillHistoricalData();
    }

    public void fillHistoricalData()
    {
        historicalInfoLabel.setText("Historical Information");
        totalTrackedTime.setText(htmlLabel("Time Tracked: ",plugin.timeConverter(fileRW.overallTime)));
        totalTrackedKills.setText(htmlLabel("Kills Tracked: ",String.valueOf(fileRW.newTotalKills)));
        actualTotalAverageKillTime.setText(htmlLabel("Average Kill: ",plugin.timeConverter(fileRW.averageKillTime)));
        actualTotalKph.setText(htmlLabel("KPH: ",String.valueOf(fileRW.killsPerHour)));
        overallFastestKill.setText(htmlLabel("Fastest Kill: ",plugin.timeConverter(fileRW.newFastestKill)));
    }

    public void closeCurrentHistoricalData()
    {
        sidePanel.remove(historicalInfoPanel);
        sidePanel.revalidate();
    }


    private JPanel buildHistoricalInfoPanel()
    {
        historicalInfoPanel.setLayout(new BorderLayout());
        historicalInfoPanel.setBorder(new EmptyBorder(0, 10, 8, 10));
        historicalInfoPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(37, 125, 141)));

        historicalInfoSection.setLayout(new GridLayout(6, 0, 0, 10));
        historicalInfoSection.setBorder(new EmptyBorder(10, 5, 10, 0));

        historicalInfoLabel.setFont(FontManager.getRunescapeBoldFont());

        //sets the continer panel to opaque or not, false = transparent, this will only affect the assingned panel. it will not affect any other content or panel within said panel.
        historicalInfoSection.setOpaque(false);

        //adds the lables to the respective panel in the order they are added
        historicalInfoSection.add(historicalInfoLabel);
        historicalInfoSection.add(actualTotalKph);
        historicalInfoSection.add(totalTrackedKills);
        historicalInfoSection.add(actualTotalAverageKillTime);
        historicalInfoSection.add(totalTrackedTime);
        historicalInfoSection.add(overallFastestKill);

        historicalInfoPanel.add(historicalInfoSection, "West");

        return historicalInfoPanel;
    }

    public void setHistoricalInfo()
    {
        if(dropdownButton.isSelected())
        {
            fillHistoricalData();
        }
    }



    private JPanel buildTitlePanel()
    {
        titlePanel.setBorder(new CompoundBorder(new EmptyBorder(5, 0, 0, 0), new MatteBorder(0, 0, 1, 0, new Color(37, 125, 141))));
        titlePanel.setLayout(new BorderLayout());
        PluginErrorPanel errorPanel = new PluginErrorPanel();
        errorPanel.setBorder(new EmptyBorder(2, 0, 3, 0));
        errorPanel.setContent("Bossing Info", "Provides various bossing statistics & utilities");
        titlePanel.add(errorPanel, "Center");
        return titlePanel;
    }




    private JPanel buildBossInfoPanel()
    {
        bossInfoPanel.setLayout(new BorderLayout());

        bossInfoPanel.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel sessionInfoSection = new JPanel(new GridBagLayout());
        sessionInfoSection.setBackground(ColorScheme.DARKER_GRAY_COLOR);


        sessionInfoSection.setLayout(new GridLayout(7, 1, 0, 10));

        sessionInfoSection.setBorder(new EmptyBorder(10, 5, 3, 0));

        currentBossNameLabel.setFont(FontManager.getRunescapeBoldFont());

        icon.setLayout(new GridLayout(0, 2, 0, 0));
        icon.setBorder(new EmptyBorder(0, 0, 180, 150));


        //sets the continer panel to opaque or not, false = transparent, this will only affect the assingned panel. it will not affect any other content or panel within said panel.
        icon.setOpaque(false);
        sessionInfoSection.setOpaque(false);


        //adds the lables to the respective panel in the order they are added
        icon.add(picLabel);
        sessionInfoSection.add(currentBossNameLabel);
        sessionInfoSection.add(killsPerHourLabel);
        sessionInfoSection.add(totalBossKillsLabel);
        sessionInfoSection.add(averageKillTimeLabel);
        sessionInfoSection.add(fastestKillTimeLabel);
        sessionInfoSection.add(idleTimeLabel);
        sessionInfoSection.add(sessionTimeLabel);

        bossInfoPanel.add(icon,"East");
        bossInfoPanel.add(sessionInfoSection, "West");


        return bossInfoPanel;
    }


    public void setBossIcon(String bossName)
    {
        if(plugin.sessionNpc != null)
        {
            KphBossInfo kphBossInfo = KphBossInfo.find(bossName);
            AsyncBufferedImage bossSprite = itemManager.getImage(kphBossInfo.getIcon());
            FontMetrics fontMetrics = getGraphics().getFontMetrics(FontManager.getRunescapeBoldFont());
            int offset = 150 - (fontMetrics.stringWidth(plugin.sessionNpc) + 10);
            icon.setBorder(new EmptyBorder(0, 0, 153,offset));
            bossSprite.addTo(picLabel);
        }
    }


    private JPanel buildButtonIconTab()
    {
        searchButton.setIcon(SEARCH_ICON);
        searchButton.setRolloverIcon(SEARCH_ICON_HOVER);

        dropdownButton.setIcon(DROPDOWN_ICON);
        dropdownButton.setRolloverIcon(DROPDOWN_HOVER);
        dropdownButton.setSelectedIcon(DROPDOWN_FLIPPED_ICON);
        dropdownButton.setRolloverSelectedIcon(DROPDOWN_FLIPPED_HOVER);


        dropdownButton.setPreferredSize(new Dimension(35, 20));
        dropdownButton.setMaximumSize(new Dimension(35, 20));
        dropdownButton.setMinimumSize(new Dimension(35, 20));

        dropdownButton.setToolTipText("Opens Historical Info");

        //removes the box around the button
        SwingUtil.removeButtonDecorations(dropdownButton);
        SwingUtil.removeButtonDecorations(searchButton);

        //links button press to method call
        dropdownButton.addActionListener((e) -> {
            if (dropdownButton.isSelected())
            {
                openCurrentHistoricalData();
            }
            else if (!dropdownButton.isSelected())
            {
                closeCurrentHistoricalData();
            }

        });

        searchButton.addActionListener(e -> openBossLookupPanels());
        searchButton.setToolTipText("Open Boss Lookup");


        buttonIconTab.setLayout(new BorderLayout());
        buttonIconTab.setBorder(new EmptyBorder(0, 0, 0, 0));
        buttonIconTab.setBorder(new MatteBorder(0, 0, 1, 0, new Color(37, 125, 141)));

        buttonIconTab.setOpaque(false);

        buttonIconTab.add(dropdownButton,"West");
        buttonIconTab.add(searchButton,"East");

        return buttonIconTab;
    }




    private JPanel buildPauseAndResumebuttons()
    {
        pauseAndResumeButtons.setLayout(new BorderLayout());

        pauseAndResumeButtons.setBorder(new EmptyBorder(4, 5, 0, 10));

        JPanel myButtons = new JPanel(new GridBagLayout());

        myButtons.setLayout(new GridLayout(1, 2, 5, 0));

        myButtons.setBorder(new EmptyBorder(5, 5, 0, 0));

        switchModeButton = new JButton("     Actual     ");

        switchModeButton.setToolTipText("Switches your information display mode");
        pauseResumeButton = new JButton("      Pause     ");


        pauseResumeButton.addActionListener((e) ->
        {
            if (plugin.paused)
            {
                plugin.sessionResume();
            }
            else
            {
                plugin.sessionPause();
            }
        });

        switchModeButton.addActionListener((e) ->
        {

            switch (plugin.getCalcMode())
            {
                case 0:
                    plugin.setCalcMode(1);
                    switchModeButton.setText("    Virtual     ");
                    plugin.calcKillsPerHour();
                    switchHistoricalInfo();
                    updateLookupInfo();
                    updateBossGoalsPanel();
                    break;
                case 1:
                    plugin.setCalcMode(0);
                    switchModeButton.setText("     Actual     ");
                    plugin.calcKillsPerHour();
                    switchHistoricalInfo();
                    updateLookupInfo();
                    updateBossGoalsPanel();
                    break;

            }

        });


        myButtons.add(pauseResumeButton);
        myButtons.add(switchModeButton);

        pauseAndResumeButtons.add(myButtons, "West");


        return pauseAndResumeButtons;
    }

    public void switchHistoricalInfo()
    {
        if(plugin.sessionNpc != null)
        {
            fileRW.statConverter();
            setHistoricalInfo();
        }

    }




    //uses the default browser on the machine to open the attached link (my discord for support & my github)
    public void discordLink()
    {
        try { Desktop.getDesktop().browse(new URI("https://discord.gg/ATXSsbbXQV")); }
        catch (IOException | URISyntaxException e1) { e1.printStackTrace(); }
    }

    public void githubLink()
    {
        try { Desktop.getDesktop().browse(new URI("https://github.com/Mrnice98/KillsPerHour")); }
        catch (IOException | URISyntaxException e1) { e1.printStackTrace(); }
    }



    private JPanel buildSessionEndButton()
    {
        this.sessionEndButton.setLayout(new BorderLayout());

        this.sessionEndButton.setBorder(new EmptyBorder(0, 5, 8, 10));

        //adds a matte border
        this.sessionEndButton.setBorder(new MatteBorder(0, 0, 1, 0, new Color(37, 125, 141)));

        JPanel myButton = new JPanel(new GridBagLayout());

        myButton.setLayout(new GridLayout(1, 2, 5, 5));

        myButton.setBorder(new EmptyBorder(3, 10, 8, 0));

        JButton endButton = new JButton("                 End Session                  ");

        endButton.addActionListener(e -> plugin.sessionEnd());

        myButton.add(endButton);

        this.sessionEndButton.add(myButton, "West");

        return this.sessionEndButton;
    }



    private JPanel buildSupportbuttons()
    {
        //sets the main panles layout
        supportButtons.setLayout(new BorderLayout());
        //sets the main panels border
        supportButtons.setBorder(new EmptyBorder(4, 5, 0, 10));

        //creates the sub panel which the buttons are contained in
        JPanel myButtons = new JPanel(new GridBagLayout());
        myButtons.setLayout(new GridLayout(1, 2, 8, 0));
        myButtons.setBorder(new EmptyBorder(10, 5, 0, 0));

        //creates the individual buttons and assings there text or icon ect...
        JButton discordButton = new JButton(DISCORD_ICON);
        JButton githubButton = new JButton(GITHUB_ICON);

        //sets what happens when the botton is hovered over
        discordButton.setRolloverIcon(DISCORD_HOVER);
        githubButton.setRolloverIcon(GITHUB_HOVER);

        //sets the buttons prefered size (can be finkiky)
        discordButton.setPreferredSize(new Dimension(23, 25));
        githubButton.setPreferredSize(new Dimension(20, 23));

        //removes the box around the button
        SwingUtil.removeButtonDecorations(githubButton);
        SwingUtil.removeButtonDecorations(discordButton);

        //links button press to method call
        githubButton.addActionListener(e -> githubLink());
        discordButton.addActionListener(e -> discordLink());

        //adds buttons to JPanel
        myButtons.add(githubButton);
        myButtons.add(discordButton);

        //adds Panel to master/main panel
        supportButtons.add(myButtons, "East");

        return supportButtons;
    }




    public void setSessionTimeLabel()
    {
        SwingUtilities.invokeLater(() -> sessionTimeLabel.setText(htmlLabel("Session time: ",plugin.timeConverter(plugin.totalSessionTime))));
    }

    public void setSessionInfo()
    {
        if(plugin.sessionNpc != null)
        {
            killsPerHourLabel.setText(htmlLabel("KPH: ",plugin.formatKPH()));
            averageKillTimeLabel.setText(htmlLabel("Average Kill: ",plugin.avgKillTimeConverter()));
            totalBossKillsLabel.setText(htmlLabel("Kills: ",String.valueOf(plugin.killsThisSession)));
            idleTimeLabel.setText(htmlLabel("Idle Time: ",plugin.timeConverter(plugin.timeSpentIdle)));
            currentBossNameLabel.setText(plugin.sessionNpc);
            fastestKillTimeLabel.setText(htmlLabel("Fastest Kill: ",plugin.timeConverter(plugin.fastestKill)));
            if(!plugin.paused)
            {
                currentBossNameLabel.setForeground(new Color(71, 226, 12));
            }

        }

        if(plugin.sessionNpc == null && plugin.cacheHasInfo)
        {
            killsPerHourLabel.setText(htmlLabel("KPH: ",plugin.cachedKPH));
            averageKillTimeLabel.setText(htmlLabel("Average Kill: ", plugin.cachedAvgKillTime));
            totalBossKillsLabel.setText(htmlLabel("Kills: ", String.valueOf(plugin.cachedSessionKills)));
            idleTimeLabel.setText(htmlLabel("Idle Time: ", plugin.cachedIdleTime));
            currentBossNameLabel.setText(plugin.cachedSessionNpc);
            fastestKillTimeLabel.setText(htmlLabel("Fastest Kill: ",plugin.cachedFastestKill));
            currentBossNameLabel.setForeground(new Color(187, 187, 187));
        }

    }


    public void updateKphMethod()
    {
        killsPerHourLabel.setText("KPH: " + plugin.formatKPH());
    }

    public void setBossNameColor()
    {
        currentBossNameLabel.setForeground(new Color(227, 160, 27));
    }

    static String htmlLabel(String key, String valueStr)
    {
        return String.format(HTML_LABEL_TEMPLATE, ColorUtil.toHexColor(ColorScheme.LIGHT_GRAY_COLOR), key, valueStr);
    }

    static
    {
        BufferedImage trashPNG = ImageUtil.loadImageResource(KphPlugin.class, "/trash_icon.png");
        BufferedImage closePNG = ImageUtil.loadImageResource(KphPlugin.class, "/close_icon.png");
        BufferedImage searchPNG = ImageUtil.loadImageResource(KphPlugin.class, "/search_icon.png");
        BufferedImage dropdownFlippedPNG = ImageUtil.loadImageResource(KphPlugin.class, "/dropdown_flipped_icon.png");
        BufferedImage dropdownPNG = ImageUtil.loadImageResource(KphPlugin.class, "/dropdown_icon.png");
        BufferedImage discordPNG = ImageUtil.loadImageResource(KphPlugin.class, "/discord_icon.png");
        BufferedImage githubPNG = ImageUtil.loadImageResource(KphPlugin.class, "/github_icon.png");
        BufferedImage visiblePNG = ImageUtil.loadImageResource(KphPlugin.class, "/visible_icon.png");
        BufferedImage invisiblePNG = ImageUtil.loadImageResource(KphPlugin.class, "/invisible_icon.png");

        VISIBLE_ICON = new ImageIcon(visiblePNG);
        INVISABLE_ICON = new ImageIcon(invisiblePNG);

        TRASH_ICON = new ImageIcon(trashPNG);
        TRASH_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(trashPNG, -80));

        CLOSE_ICON = new ImageIcon(closePNG);
        CLOSE_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(closePNG, -80));

        SEARCH_ICON = new ImageIcon(searchPNG);
        SEARCH_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(searchPNG, -80));

        DROPDOWN_FLIPPED_ICON = new ImageIcon(dropdownFlippedPNG);
        DROPDOWN_FLIPPED_HOVER = new ImageIcon(ImageUtil.luminanceOffset(dropdownFlippedPNG, -80));

        DROPDOWN_ICON = new ImageIcon(dropdownPNG);
        DROPDOWN_HOVER = new ImageIcon(ImageUtil.luminanceOffset(dropdownPNG, -80));

        DISCORD_ICON = new ImageIcon(discordPNG);
        DISCORD_HOVER = new ImageIcon(ImageUtil.luminanceOffset(discordPNG, -80));

        GITHUB_ICON = new ImageIcon(githubPNG);
        GITHUB_HOVER = new ImageIcon(ImageUtil.luminanceOffset(githubPNG, -80));

    }

}
