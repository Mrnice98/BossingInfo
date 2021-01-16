/* Copyright (c) 2020, MrNice98
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


import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

class KphPanel extends PluginPanel {

    private final JTextField searchField = new JTextField();

    private final JLabel historicalInfoLabel = new JLabel();
    private final JLabel totalTrackedTime = new JLabel();
    private final JLabel totalTrackedKills = new JLabel();
    private final JLabel actualTotalAverageKillTime = new JLabel();
    private final JLabel actualTotalKph = new JLabel();
    private final JLabel overallFastestKill = new JLabel();
    private final JLabel sessionTimeLabel = new JLabel("Session Time: N/A");
    private final JLabel totalBossKillsLabel = new JLabel("Kills: N/A");
    private final JLabel averageKillTimeLabel = new JLabel("Average Kill: N/A");
    private final JLabel fastestKillTimeLabel = new JLabel("Fastest Kill: N/A");
    private final JLabel killsPerHourLabel = new JLabel("KPH: N/A");
    private final JLabel idleTimeLabel = new JLabel("Idle Time: N/A");
    private final JLabel fetchedBossName = new JLabel("Unknown Boss");
    private final JLabel fetchedTotalTimeActual = new JLabel("Total Session Time: N/A");
    private final JLabel fetchedTotalTimeVirtual = new JLabel("Total Boss Time: N/A");
    private final JLabel fetchedTotalTrackedKills = new JLabel("Tracked Kills: N/A");
    private final JLabel fetchedFastestKill = new JLabel("Fastest Kill: N/A");
    private final JLabel fetchedKillsPerHour = new JLabel("KPH: N/A");
    private final JLabel fetchedTotalBossKc = new JLabel("Total KC: N/A");
    private final JLabel fetchedAverageKillTime = new JLabel("Average Kill: N/A");
    private final JLabel estimatedTimeSpentBossing = new JLabel("EST Bossing Time: N/A");
    private final JLabel lookupHeaderTitle = new JLabel("Boss Info Lookup");
    private final JLabel searchIcon = new JLabel(SEARCH_ICON);
    private final JLabel picLabel = new JLabel();
    private final JLabel fetchedIconLabel = new JLabel();
    JLabel currentBossNameLabel = new JLabel("Unknown Boss Name");

    private final JPanel lookupPanelHeaderContents = new JPanel();
    private final JPanel historicalInfoSection = new JPanel(new GridBagLayout());
    private final JPanel buttonIconTab;
    private final JPanel icon;
    private final JPanel fetchedIcon;
    private final JPanel fetchedInfoPanel;
    private final JPanel closeAndTrashButtonPanel;
    private final JPanel sidePanel;
    private final JPanel titlePanel;
    private final JPanel bossInfoPanel;
    private final JPanel historicalInfoPanel;
    private final JPanel lookupInfoPanelHeader;
    private final JPanel pauseAndResumeButtons;
    private final JPanel sessionEndButton;
    private final JPanel supportButtons;

    private final JToggleButton dropdownButton = new JToggleButton();
    private final JButton closeButton = new JButton(CLOSE_ICON);
    private final JButton trashButton = new JButton(TRASH_ICON);
    private final JButton searchButton = new JButton();
    JButton pauseResumeButton = new JButton();
    JButton switchModeButton = new JButton();


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

    private AsyncBufferedImage fetchedBossSprite;

    private final KphConfig config;

    private final KphPlugin plugin;

    private final FileReadWriter fileRW;

    @Inject
    private ItemManager itemManager;

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
        this.sidePanel.add(this.buildButtonIconTab());
        this.sidePanel.add(this.buildPauseAndResumebuttons());
        this.sidePanel.add(this.buildSessionEndButton());
        this.sidePanel.add(this.buildSupportbuttons());

        //ensure to build all the panels on the initial load, do not build them everytime i add or remove.
        buildFetchedInfoPanel();
        buildlookupInfoPanelHeader();
        buildHistoricalInfoPanel();

        this.add(sidePanel, "North");
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




    public void fetchLookupInfo()
    {
        String searchInput = searchField.getText();

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
                fetchedIconLabel.setVisible(true);

                break;
            }
            else
            {
                fetchedBossName.setText("Unknown Boss");
                fetchedIconLabel.setVisible(false);
            }

        }


        int offset = 180 - ((fetchedBossName.getText().length() * 8) + 6) ;
                                                        //if i chan
        fetchedIcon.setBorder(new EmptyBorder(0, 0, 0,offset));

        fetchedBossSprite.addTo(fetchedIconLabel);

        updateLookupInfo();


    }

    public void updateLookupInfo()
    {
        if(!fileRW.fetchedFile.exists())
        {
            System.out.println("Lookup info set to null");
            setLookupInfoToNull();
            return;
        }

        fileRW.fetchedStatConverter();
        fetchedFastestKill.setText("Fastest Kill: " + plugin.timeConverter(fileRW.fetchedFastestKill));
        fetchedTotalTrackedKills.setText("Tracked Kills: " + fileRW.fetchedTotalTrackedKills);
        fetchedTotalTimeActual.setText("Total Session Time: " + plugin.timeConverter(fileRW.fetchedTotalTimeActual));
        fetchedTotalTimeVirtual.setText("Total Boss Time: " + plugin.timeConverter(fileRW.fetchedTotalTimeVirtual));
        fetchedKillsPerHour.setText("KPH: " + fileRW.fetchedKillsPerHour);
        fetchedTotalBossKc.setText("Total KC: " + fileRW.fetchedTotalBossKc);
        fetchedAverageKillTime.setText("Average Kill: " + plugin.timeConverter(fileRW.fetchedAverageKillTime));
        estimatedTimeSpentBossing.setText("EST Bossing Time: " + plugin.timeConverter(fileRW.estimatedTimeSpentBossing));
    }


    public void setLookupInfoToNull()
    {
        fetchedFastestKill.setText("Fastest Kill: N/A");
        fetchedTotalTrackedKills.setText("Tracked Kills: N/A" );
        fetchedTotalTimeActual.setText("Total Session Time: N/A" );
        fetchedTotalTimeVirtual.setText("Total Boss Time: N/A");
        fetchedKillsPerHour.setText("KPH: N/A");
        fetchedTotalBossKc.setText("Total KC: N/A");
        fetchedAverageKillTime.setText("Average Kill: N/A");
        estimatedTimeSpentBossing.setText("EST Bossing Time: N/A");
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


    public void deleteFile()
    {
        if(fileRW.fetchedFile.exists())
        {
            int confirm = JOptionPane.showConfirmDialog(
                    KphPanel.this,
                    "Are you sure you want to permanently delete " + fileRW.fetchedFile.getName(),
                    "Warning", JOptionPane.OK_CANCEL_OPTION);

            if (confirm == 0)
            {
                try
                {
                    Files.delete(fileRW.lookupPath);
                }
                catch (IOException e)
                {
                    System.out.println("File could not be deleted");
                    System.out.println(fileRW.lookupPath.toString());
                }
            }
        }
        else
        {
            System.out.println("File dose not exist");
        }

    }


    public void closeBossLookupPanels()
    {
        sidePanel.remove(fetchedInfoPanel);
        sidePanel.remove(lookupInfoPanelHeader);
        sidePanel.add(bossInfoPanel);
        sidePanel.add(buttonIconTab);
        sidePanel.setComponentZOrder(bossInfoPanel,2);
        sidePanel.setComponentZOrder(buttonIconTab,3);
        sidePanel.revalidate();
    }

    public void openBossLookupPanels()
    {
        sidePanel.remove(buttonIconTab);
        sidePanel.remove(bossInfoPanel);
        sidePanel.remove(historicalInfoPanel);
        sidePanel.add(lookupInfoPanelHeader);
        sidePanel.add(fetchedInfoPanel);
        sidePanel.setComponentZOrder(lookupInfoPanelHeader,2);
        sidePanel.setComponentZOrder(fetchedInfoPanel,3);
        sidePanel.setComponentZOrder(pauseAndResumeButtons,4);
        dropdownButton.setSelected(false);
        sidePanel.revalidate();
    }


    public void openCurrentHistoricalData()
    {

        sidePanel.add(historicalInfoPanel);
        sidePanel.setComponentZOrder(historicalInfoPanel,4);
        sidePanel.revalidate();
        fillHistoricalData();

    }

    public void fillHistoricalData()
    {
        historicalInfoLabel.setText("Historical Information");
        totalTrackedTime.setText("Time Tracked: " + plugin.timeConverter(fileRW.overallTime));
        totalTrackedKills.setText("Kills Tracked: " + fileRW.newTotalKills);
        actualTotalAverageKillTime.setText("Average Kill: " + plugin.timeConverter(fileRW.averageKillTime));
        actualTotalKph.setText("KPH: " + fileRW.killsPerHour);
        overallFastestKill.setText("Fastest Kill: " + plugin.timeConverter(fileRW.newFastestKill));
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
        errorPanel.setContent("KPH Tracker", "Tracks your KPH at various bosses");
        titlePanel.add(errorPanel, "Center");
        return titlePanel;
    }


    private JPanel buildBossInfoPanel()
    {
        bossInfoPanel.setLayout(new BorderLayout());

        bossInfoPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        bossInfoPanel.setBorder(new MatteBorder(0, 0, 0, 0, new Color(37, 125, 141)));

        JPanel sessionInfoSection = new JPanel(new GridBagLayout());

        sessionInfoSection.setLayout(new GridLayout(7, 1, 0, 10));

        //this controls the offset of the current boss name, useful for alinging the icon
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
            //this is how the icon is positioned
            int offset = 150 - ((plugin.sessionNpc.length() * 8) + 6) ;
                                                            //if i change the bottom offset i can compensate when changeing the row number of the session info
            icon.setBorder(new EmptyBorder(0, 0, 153,offset));

            //use this method when applying icons.
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
                    updateLookupInfo();
                    switchHistoricalInfo();
                    break;
                case 1:
                    plugin.setCalcMode(0);
                    switchModeButton.setText("     Actual     ");
                    plugin.calcKillsPerHour();
                    updateLookupInfo();
                    switchHistoricalInfo();
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
        sessionTimeLabel.setText("Session time: " + plugin.timeConverter(plugin.totalSessionTime));
    }

    public void setSessionInfo()
    {
        if(plugin.sessionNpc != null)
        {
            killsPerHourLabel.setText("KPH: " + plugin.formatKPH());
            averageKillTimeLabel.setText("Average Kill: " + plugin.avgKillTimeConverter());
            totalBossKillsLabel.setText("Kills: " + plugin.killsThisSession);
            idleTimeLabel.setText("Idle Time: " + plugin.timeConverter(plugin.timeSpentIdle));
            currentBossNameLabel.setText(plugin.sessionNpc);
            fastestKillTimeLabel.setText("Fastest Kill: " + plugin.timeConverter(plugin.fastestKill));

            if(!plugin.paused)
            {
                currentBossNameLabel.setForeground(new Color(71, 226, 12));
            }

        }

        if(plugin.sessionNpc == null && plugin.cacheHasInfo)
        {
            killsPerHourLabel.setText("KPH: " + plugin.cachedKPH);
            averageKillTimeLabel.setText("Average Kill: " + plugin.cachedAvgKillTime);
            totalBossKillsLabel.setText("Kills: " + plugin.cachedSessionKills);
            idleTimeLabel.setText("Idle Time: " + plugin.cachedIdleTime);
            currentBossNameLabel.setText(plugin.cachedSessionNpc);
            fastestKillTimeLabel.setText("Fastest Kill: " + plugin.cachedFastestKill);
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


    static
    {
        BufferedImage trashPNG = ImageUtil.getResourceStreamFromClass(KphPlugin.class, "/trash_icon.png");
        BufferedImage closePNG = ImageUtil.getResourceStreamFromClass(KphPlugin.class, "/close_icon.png");
        BufferedImage searchPNG = ImageUtil.getResourceStreamFromClass(KphPlugin.class, "/search_icon.png");
        BufferedImage dropdownFlippedPNG = ImageUtil.getResourceStreamFromClass(KphPlugin.class, "/dropdown_flipped_icon.png");
        BufferedImage dropdownPNG = ImageUtil.getResourceStreamFromClass(KphPlugin.class, "/dropdown_icon.png");
        BufferedImage discordPNG = ImageUtil.getResourceStreamFromClass(KphPlugin.class, "/discord_icon.png");
        BufferedImage githubPNG = ImageUtil.getResourceStreamFromClass(KphPlugin.class, "/github_icon.png");

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
