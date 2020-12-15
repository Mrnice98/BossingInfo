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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;
import java.awt.image.BufferedImage;


@ToString
class KphInfobox extends InfoBox
{

	private final KphConfig config;

	private final KphPlugin plugin;

	private final BufferedImage image;

	private final OverlayPosition position;

	//this creates a setter and getter method with lombook
	@Getter
	@Setter
	private String Kph;

	KphInfobox(BufferedImage image, KphPlugin plugin, KphConfig config, OverlayPosition position)
	{
		super(image,plugin);
		this.image = image;
		this.plugin = plugin;
		this.config = config;
		this.position = position;
	}

	@Override
	public String getText()
	{
		return plugin.formatKPH();
	}

	@Override
	public Color getTextColor()
	{
		return Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		StringBuilder toolTip = new StringBuilder();

		toolTip.append(plugin.sessionNpc);

		toolTip.append("</br>KPH: ");
		toolTip.append(plugin.formatKPH());

		if(config.averageKillTime())
		{
			toolTip.append("</br>Kills: ");
			toolTip.append(plugin.killsThisSession);
		}

		if(config.averageKillTime())
		{
			toolTip.append("</br>Avg Kill: ");
			toolTip.append(plugin.timeConverter(plugin.averageKillTime));
		}

		if(config.displayIdleTime())
		{
			toolTip.append("</br>Idle time: ");
			toolTip.append(plugin.timeConverter(plugin.timeSpentIdle));
		}

		if(config.displayTotalTime())
		{
			toolTip.append("</br>Session Time: ");
			toolTip.append(plugin.timeConverter(plugin.totalSessionTime));
		}


		return toolTip.toString();
	}



}