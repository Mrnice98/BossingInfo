package com.killsperhour;

import com.killsperhour.KphConfig;
import com.killsperhour.KphPlugin;

import javax.inject.Inject;
import java.time.Instant;

public class KphSpecialMethods
{
    @Inject
    private KphPlugin plugin;

    @Inject
    private KphConfig config;





//                                                  KRAKEN SECTION
//##################################################################################################################################

    public boolean krakenChecker()
    {
        if(plugin.lastValidBoss.getName() != null)
        {
            return (plugin.lastValidBoss.getName().equals("Kraken") || plugin.lastValidBoss.getName().equals("Enormous Tentacle"));
        }
        else
        {
            return false;
        }
    }

    public Instant krakenTimeClac()
    {
        if(plugin.lastValidBoss.getName() != null)
        {
            if(krakenChecker())
            {
                if(plugin.krakenStart == null)
                {
                    plugin.krakenStart = Instant.now();
                }
                return plugin.krakenStart;
            }
        }
        return null;
    }

    public void krakenTimeClear()
    {
        plugin.krakenStart = null;
    }


//                                              SIRE SECTION
//###################################################################################################################

    public boolean sireChecker()
    {
        if(plugin.lastValidBoss.getName() != null)
        {
            return (plugin.lastValidBoss.getName().equals("Abyssal Sire"));
        }
        else
        {
            return false;
        }
    }

    public void sireTimeClac()
    {
        if(plugin.message.contains("The Sire has been disorientated temporarily."))
        {
            if(plugin.sireStart == null)
            {
                plugin.sireStart = Instant.now();
            }
            plugin.lastAttkTimeout = 0;
            plugin.attkTimeout = 300;
        }
    }

    public void sireTimeClear()
    {
        plugin.sireStart = null;
    }


//                                                    BARROWS SECTION
//##################################################################################################################################



    public Instant barrowsTimeClac()
    {
        if(plugin.lastValidBoss.getName() != null)
        {
            if(barrowsChecker())
            {
                if(plugin.barrowsStart == null)
                {
                    plugin.barrowsStart = Instant.now();
                }
                return plugin.barrowsStart;

            }

        }
        return null;
    }

    public boolean barrowsChecker()
    {
        if(plugin.lastValidBoss.getName() != null)
        {
            return (plugin.lastValidBoss.getName().equals("Ahrim the Blighted")
                 || plugin.lastValidBoss.getName().equals("Dharok the Wretched")
                 || plugin.lastValidBoss.getName().equals("Guthan the Infested")
                 || plugin.lastValidBoss.getName().equals("Karil the Tainted")
                 || plugin.lastValidBoss.getName().equals("Torag the Corrupted")
                 || plugin.lastValidBoss.getName().equals("Verac the Defiled"));
        }
        else
        {
            return false;
        }
    }

    public void barrowsTimeClear()
    {
        plugin.barrowsStart = null;
    }


//                                               DAGGANOTH SECTION
//##################################################################################################################################


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

        if(plugin.message.contains(plugin.rexMessage) || plugin.rexAttkTimout == 10)
        {
            plugin.rexStart = null;
        }
        if(plugin.message.contains(plugin.primeMessage) || plugin.primeAttkTimout == 10)
        {
            plugin.primeStart = null;
        }
        if(plugin.message.contains(plugin.supremeMessage) || plugin.supremeAttkTimout == 10)
        {
            plugin.supremeStart = null;
        }
    }


    //should work but needs testing ********************************************
    public void dagTimeClearTwo()
    {
        if(plugin.lastValidBoss.getName() != null)
        {
            switch (plugin.lastValidBoss.getName())
            {
                case "Dagannoth Prime":
                    plugin.primeStart = null;
                    break;

                case "Dagannoth Rex":
                    plugin.rexStart = null;
                    break;

                case "Dagannoth Supreme":
                    plugin.supremeStart = null;
                    break;
            }
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
