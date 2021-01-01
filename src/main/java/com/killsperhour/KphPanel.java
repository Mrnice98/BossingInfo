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

class KphPanel extends PluginPanel {
    final JLabel currentBossNameLabel = new JLabel("Unknown Boss Name");
    private final JLabel sessionTimeLabel = new JLabel("Session Time: N/A");
    private final JLabel totalBossKillsLabel = new JLabel("Kills: N/A");
    private final JLabel averageKillTimeLabel = new JLabel("Average Kill: N/A");
    private final JLabel fastestKillTimeLabel = new JLabel("Fastest Kill: N/A");
    private final JLabel killsPerHourLabel = new JLabel("KPH: N/A");
    private final JLabel idleTimeLabel = new JLabel("Idle Time: N/A");
    private final JLabel picLabel = new JLabel();

    private final JPanel icon;
    private final JPanel sidePanel;
    private final JPanel titlePanel;
    private final JPanel bossInfoPanel;
    private final JPanel pauseAndResumeButtons;
    private final JPanel sessionEndButton;
    private final JPanel supportButtons;

    private static final ImageIcon DISCORD_ICON;
    private static final ImageIcon  DISCORD_HOVER;
    private static final ImageIcon GITHUB_ICON;
    private static final ImageIcon  GITHUB_HOVER;

    private final KphConfig config;

    private final KphPlugin plugin;

    @Inject
    private ItemManager itemManager;

    JButton pauseResumeButton = new JButton();
    JButton switchModeButton = new JButton();


    @Inject
    KphPanel(KphPlugin plugin, KphConfig config)
    {
        this.sessionEndButton = new JPanel();
        this.pauseAndResumeButtons = new JPanel();
        this.supportButtons = new JPanel();
        this.sidePanel = new JPanel();
        this.titlePanel = new JPanel();
        this.bossInfoPanel = new JPanel();
        this.icon = new JPanel();
        this.plugin = plugin;
        this.config = config;
    }

    void sidePanelInitializer()
    {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.sidePanel.setLayout(new BoxLayout(this.sidePanel, 1));
        this.sidePanel.add(this.buildTitlePanel());
        this.sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        this.sidePanel.add(this.buildBossInfoPanel());
        this.sidePanel.add(this.buildPauseAndResumebuttons());
        this.sidePanel.add(this.buildSessionEndButton());
        this.sidePanel.add(this.buildSupportbuttons());
        this.add(sidePanel, "North");
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

        bossInfoPanel.setBorder(new EmptyBorder(0, 10, 8, 10));

        bossInfoPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(37, 125, 141)));

        JPanel sessionInfoSection = new JPanel(new GridBagLayout());

        sessionInfoSection.setLayout(new GridLayout(8, 1, 0, 10));

        //this controls the offset of the current boss name, useful for alinging the icon
        sessionInfoSection.setBorder(new EmptyBorder(10, 5, 4, 0));

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


    public void setBossIcon()
    {
        if(plugin.sessionNpc != null)
        {
            KphBossInfo kphBossInfo = KphBossInfo.find(plugin.sessionNpc);
            AsyncBufferedImage bossSprite = itemManager.getImage(kphBossInfo.getIcon());
            //this is how the icon is positioned
            int offset = 150 - ((plugin.sessionNpc.length() * 8) + 6) ;
            icon.setBorder(new EmptyBorder(0, 0, 180,offset));

            //use this method when applying icons.
            bossSprite.addTo(picLabel);
        }
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
                    break;
                case 1:
                    plugin.setCalcMode(0);
                    switchModeButton.setText("     Actual     ");
                    plugin.calcKillsPerHour();
                    break;
            }

        });


        myButtons.add(pauseResumeButton);
        myButtons.add(switchModeButton);

        pauseAndResumeButtons.add(myButtons, "West");


        return pauseAndResumeButtons;
    }




    //uses the defualt browser on the machine to open the attached link (my discord for support & my github)
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
            averageKillTimeLabel.setText("Average Kill Time: " + plugin.avgKillTimeConverter());
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
            averageKillTimeLabel.setText("Average Kill Time: " + plugin.cachedAvgKillTime);
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
        BufferedImage discordPNG = ImageUtil.getResourceStreamFromClass(KphPlugin.class, "/discord_icon.png");
        BufferedImage githubPNG = ImageUtil.getResourceStreamFromClass(KphPlugin.class, "/github_icon.png");
        DISCORD_ICON = new ImageIcon(discordPNG);
        DISCORD_HOVER = new ImageIcon(ImageUtil.luminanceOffset(discordPNG, -80));
        GITHUB_ICON = new ImageIcon(githubPNG);
        GITHUB_HOVER = new ImageIcon(ImageUtil.luminanceOffset(githubPNG, -80));

    }

}
