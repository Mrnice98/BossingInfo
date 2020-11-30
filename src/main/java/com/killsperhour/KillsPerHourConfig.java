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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("averagetime")
public interface KillsPerHourConfig extends Config
{

    @ConfigSection(
            name = "Display Options",
            description = "All Display Options to Choose From",
            position = 0,
            closedByDefault = true
    )
    String displaySection = "Display Options";



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
            keyName = "Average Kill Time",
            name = "Average Kill Time",
            description = "Display Average Kill Time",
            section = displaySection
    )
    default boolean averageKillTime() { return false; }

    @ConfigItem(
            position = 2,
            keyName = "Kills This Session",
            name = "Kills This Session",
            description = "Display Kills This Session",
            section = displaySection
    )
    default boolean killsThisSession() { return false; }

    @ConfigItem(
            position = 3,
            keyName = "Display Session Time",
            name = "Session Time",
            description = "Displays a running count of the session time ",
            section = displaySection
    )
    default boolean displayTotalTime() { return false; }

    @ConfigItem(
            position = 4,
            keyName = "Display Banking Time",
            name = "Banking Time",
            description = "Toggles the display for banking time, Only works if 'Account for Banking' is enabled ",
            section = displaySection
    )
    default boolean displayBankingTime() { return false; }

//#######################################################################################################################


    //bosses with no chat display essentially account for banking regardless.
    @ConfigItem(
            position = 1,
            keyName = "Account For Banking",
            name = "Account for Banking",
            description = "Take banking/downtime into account. " +
                          "Changing this option will reset session on the next kill. " + "Only effects bosses who display times in chat"
    )
    default boolean accountForBank() { return false; }



    @ConfigItem(
            position = 9,
            keyName = "Output Info On Session Change",
            name = "Output Info",
            description = "Outputs session info when session is ended or switched"

    )
    default boolean outputOnChange() { return false; }



    @ConfigItem(
            position = 10,
            keyName = "Session Timeout",
            name = "Session Timeout",
            description = "Set the session timeout time in minutes (set to 0 for no timeout time)"
    )
    default int timeoutTime() { return 0; }


    //triditional = integer math, same as round down
    enum KphMethod
    {
        PRECISE,
        ROUNDED,
        ROUND_UP,
        TRADITIONAL
    }

    @ConfigItem(
            keyName = "KPH Calc Method",
            name = "KPH Calc",
            description = "Allows you to choose the method KPH calculated via",
            position = 11
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
            keyName = "Dagannoth Selector",
            name = "Dagannoth Selector",
            description = "Allows you to select which Dagannoth King the plugin will track",
            position = 12
    )
    default DksSelector dksSelector() { return DksSelector.Kings; }




}
