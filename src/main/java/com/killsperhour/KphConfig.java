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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("averagetime")
public interface KphConfig extends Config
{

    @ConfigSection(
            name = "Display Options",
            description = "All Display Options to Choose From",
            position = 0,
            closedByDefault = true
    )
    String displaySection = "Display Options";

    @ConfigSection(
            name = "General Settings",
            description = "General Settings",
            position = 1,
            closedByDefault = false
    )
    String generalSettings = "General Settings";



//                                    DISPLAY SECTION
//#########################################################################################################
    @ConfigItem(
            position = 0,
            keyName = "Enable Overlay",
            name = "Enable Overlay",
            description = "Enables the overlay",
            section = displaySection

    )
    default boolean enableOverlay() { return true; }

    @ConfigItem(
            position = 1,
            keyName = "Display Infobox",
            name = "Display Infobox",
            description = "Enables the infobox",
            section = displaySection

    )
    default boolean renderInfobox() { return false; }

    @ConfigItem(
            position = 2,
            keyName = "Average Kill Time",
            name = "Average Kill Time",
            description = "Display Average Kill Time",
            section = displaySection
    )
    default boolean averageKillTime() { return false; }

    @ConfigItem(
            position = 3,
            keyName = "Kills This Session",
            name = "Kills This Session",
            description = "Display Kills This Session",
            section = displaySection
    )
    default boolean killsThisSession() { return false; }

    @ConfigItem(
            position = 4,
            keyName = "Session Time",
            name = "Session Time",
            description = "Displays a running count of the session time ",
            section = displaySection
    )
    default boolean displayTotalTime() { return false; }

    @ConfigItem(
            position = 5,
            keyName = "Idle Time",
            name = "Idle Time",
            description = "Toggles the display for Idle time, Only works if 'Account for Idle' is enabled ",
            section = displaySection
    )
    default boolean displayIdleTime() { return false; }

//#######################################################################################################################


//                                        GENERAL SECTION
//#######################################################################################################################


    @ConfigItem(
            position = 0,
            keyName = "Side Panel",
            name = "Side Panel",
            description = "Enables the side panel",
            section = generalSettings
    )
    default boolean showSidePanel() { return true; }

    @ConfigItem(
            position = 1,
            keyName = "Kill Duration",
            name = "Kill Duration",
            description = "Upon a kill a chat message will be added with your kill time",
            section = generalSettings

    )
    default boolean displayKillTimes() { return false; }

    @ConfigItem(
            position = 2,
            keyName = "Output Info",
            name = "Output Info",
            description = "Outputs session info when session is ended or switched",
            section = generalSettings
    )
    default boolean outputOnChange() { return false; }

    @ConfigItem(
            position = 3,
            keyName = "Account For Idle time",
            name = "Account for Idle time",
            description = "Take Idle time into account with calculations. Changing this option will reset session on the next kill.",
            section = generalSettings
    )
    default boolean accountForIdle() { return false; }


    @ConfigItem(
            position = 4,
            keyName = "Session Timeout",
            name = "Session Timeout",
            description = "Set the session timeout time in minutes (set to 0 for no timeout time)",
            section = generalSettings
    )
    default int timeoutTime() { return 0; }


    //traditional = integer math, same as round down
    enum KphMethod
    {
        PRECISE,
        ROUNDED,
        ROUND_UP,
        TRADITIONAL
    }

    @ConfigItem(
            position = 5,
            keyName = "KPH Calc Method",
            name = "KPH Calc",
            description = "Allows you to choose the method KPH calculated via",
            section = generalSettings
    )
    default KphMethod kphMethod() { return KphMethod.PRECISE; }

    enum DksSelector
    {
        Rex,
        Prime,
        Supreme,
        Kings
    }

    @ConfigItem(
            position = 6,
            keyName = "Dagannoth Selector",
            name = "Dagannoth Selector",
            description = "Allows you to select which Dagannoth King the plugin will track",
            section = generalSettings
    )
    default DksSelector dksSelector() { return DksSelector.Kings; }




}
