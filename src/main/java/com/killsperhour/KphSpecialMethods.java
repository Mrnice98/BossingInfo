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

import javax.inject.Inject;
import java.time.Instant;

public class KphSpecialMethods
{
    @Inject
    private KphPlugin plugin;

    @Inject
    private KphConfig config;


    public Instant dagTimeClac()
    {
        if(plugin.lastValidBoss.getName() != null)
        {
            switch (plugin.lastValidBoss.getName())
            {
                case "Dagannoth Prime":
                    plugin.primeAttkTimout = 0;
                    if(plugin.primeStart == null)
                    {
                        plugin.primeStart = Instant.now();
                    }
                    return plugin.primeStart;

                case "Dagannoth Rex":
                    plugin.rexAttkTimout = 0;
                    if(plugin.rexStart == null)
                    {
                        plugin.rexStart = Instant.now();
                    }
                    return plugin.rexStart;

                case "Dagannoth Supreme":
                    plugin.supremeAttkTimout = 0;
                    if(plugin.supremeStart == null)
                    {
                        plugin.supremeStart = Instant.now();
                    }
                    return plugin.supremeStart;

                default:
                    return null;
            }
        }
        return null;
    }


    public boolean dagChecker()
    {
        if(plugin.lastValidBoss.getName() != null)
        {
            return (plugin.lastValidBoss.getName().equals("Dagannoth Prime") || plugin.lastValidBoss.getName().equals("Dagannoth Rex") || plugin.lastValidBoss.getName().equals("Dagannoth Supreme"));
        }
        else
        {
            return false;
        }
    }



    public void dagTimeClear()
    {

        if(plugin.message.contains(plugin.rexMessage) || plugin.rexAttkTimout == 20)
        {
            plugin.rexStart = null;
        }
        if(plugin.message.contains(plugin.primeMessage) || plugin.primeAttkTimout == 20)
        {
            plugin.primeStart = null;
        }
        if(plugin.message.contains(plugin.supremeMessage) || plugin.supremeAttkTimout == 20)
        {
            plugin.supremeStart = null;
        }
    }

    public boolean dagKingsCheck()
    {
        if(plugin.lastValidBoss.getName() != null)
        {
            return (plugin.lastValidBoss.getName().equals("Dagannoth Prime") || plugin.lastValidBoss.getName().equals("Dagannoth Rex") || plugin.lastValidBoss.getName().equals("Dagannoth Supreme")) && (config.dksSelector() == KphConfig.DksSelector.Kings);
        }
        else
        {
            return false;
        }
    }


}
