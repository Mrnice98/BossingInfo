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

class KphOverlay extends Overlay
{
    private final Client client;
    private final KphConfig config;
    private final KphPanel panel;
    private final KphPlugin kphPlugin;
    private final PanelComponent panelComponent = new PanelComponent();


    @Inject
    private KphOverlay(Client client, KphConfig config, KphPlugin kphPlugin, KphPanel panel)
    {
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        this.client = client;
        this.config = config;
        this.kphPlugin = kphPlugin;
        this.panel = panel;
    }


    @Override
    public Dimension render(Graphics2D graphics)
    {
            panelComponent.getChildren().clear();
            String overlayTitle = kphPlugin.currentBoss;

            if(!kphPlugin.paused)
            {
                panelComponent.getChildren().add(TitleComponent.builder()
                        .text(overlayTitle)
                        .color(Color.GREEN)
                        .build());
            }
            else
            {
                panelComponent.getChildren().add(TitleComponent.builder()
                        .text(overlayTitle)
                        .color(new Color(227, 160, 27))
                        .build());
            }

            panelComponent.setPreferredSize(new Dimension(
                    150,
                    0));


            panelComponent.getChildren().add(LineComponent.builder()
                    .left("KPH:")
                    .right(kphPlugin.formatKPH())
                    .build());


        if(config.killsThisSession())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Kills:")
                    .right(Integer.toString(kphPlugin.killsThisSession))
                    .build());

        }


        if (config.averageKillTime())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Average Kill:")
                    .right(kphPlugin.avgKillTimeConverter())
                    .build());

        }


        if (config.displayIdleTime())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Idle Time:")
                    .right((kphPlugin.timeConverter(kphPlugin.timeSpentIdle)))
                    .build());
        }


        if (config.displayTotalTime())
        {
            if(!kphPlugin.paused && kphPlugin.sessionNpc != null)
            {
                kphPlugin.totalSessionTimer();
            }
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Session Time:")
                    .right((kphPlugin.timeConverter(kphPlugin.totalSessionTime)))
                    .build());
        }


        if(kphPlugin.killsThisSession >= 1 && config.enableOverlay())
        {
            return panelComponent.render(graphics);
        }

        return null;
    }


}
