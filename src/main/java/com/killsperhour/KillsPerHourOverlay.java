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

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.text.DecimalFormat;

class KillsPerHourOverlay extends Overlay
{
    private final Client client;
    private final com.killsperhour.KillsPerHourConfig config;
    private final com.killsperhour.KillsPerHourPlugin killsPerHourPlugin;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    private KillsPerHourOverlay(Client client, com.killsperhour.KillsPerHourConfig config, com.killsperhour.KillsPerHourPlugin killsPerHourPlugin)
    {
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        this.client = client;
        this.config = config;
        this.killsPerHourPlugin = killsPerHourPlugin;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {

            panelComponent.getChildren().clear();
            String overlayTitle = killsPerHourPlugin.currentBoss;

            panelComponent.getChildren().add(TitleComponent.builder()
                    .text(overlayTitle)
                    .color(Color.GREEN)
                    .build());

            panelComponent.setPreferredSize(new Dimension(
                    150,
                    0));



            // KILLS PER HOUR METHODS
            if(config.kphMethod() == com.killsperhour.KillsPerHourConfig.KphMethod.TRADITIONAL)
            {
                int Kph = (int) killsPerHourPlugin.killsPerHour;
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("KPH:")
                        .right(Integer.toString(Kph))
                        .build());

            }

            if(config.kphMethod() == com.killsperhour.KillsPerHourConfig.KphMethod.PRECISE)
            {
                DecimalFormat df = new DecimalFormat("#.#");
                double Kph = Double.parseDouble(df.format(killsPerHourPlugin.killsPerHour));
                panelComponent.getChildren().add(LineComponent.builder()
                         .left("KPH:")
                         .right(Double.toString(Kph))
                         .build());

            }

            if(config.kphMethod() == com.killsperhour.KillsPerHourConfig.KphMethod.ROUNDED)
            {
                int Kph = (int)(Math.round(killsPerHourPlugin.killsPerHour));
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("KPH:")
                        .right(Integer.toString(Kph))
                        .build());

            }

            if(config.kphMethod() == com.killsperhour.KillsPerHourConfig.KphMethod.ROUND_UP)
            {
                int Kph = (int)(Math.ceil(killsPerHourPlugin.killsPerHour));
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("KPH:")
                        .right(Integer.toString(Kph))
                        .build());

            }



        if (config.averageKillTime())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Average Kill Time:")
                    .right(killsPerHourPlugin.avgKillTimeConverter())
                    .build());

        }

        if(config.killsThisSession())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Kills this session:")
                    .right(Integer.toString(killsPerHourPlugin.killsThisSession))
                    .build());

        }

        if (config.displayBankingTime() && killsPerHourPlugin.bossType() == 1)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Bank/Idle Time:")
                    .right((killsPerHourPlugin.timeConverter(killsPerHourPlugin.timeSpentBanking)))
                    .build());
        }


        if (config.displayTotalTime())
        {
            if(!killsPerHourPlugin.paused)
            {
                killsPerHourPlugin.totalSessionTimer();
            }
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Session Time:")
                    .right((killsPerHourPlugin.timeConverter(killsPerHourPlugin.totalSessionTime)))
                    .build());


        }


        if(killsPerHourPlugin.killsThisSession >= 1 && config.enableOverlay())
        {
            return panelComponent.render(graphics);
        }

        return null;
    }
}
