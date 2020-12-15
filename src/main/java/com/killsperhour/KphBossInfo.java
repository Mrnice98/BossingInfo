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

import net.runelite.api.ItemID;
import javax.inject.Inject;



public class KphBossInfo
{
   
    @Inject
    private KphPlugin plugin;

        public void registerBossLists()
        {
           plugin.bossIcon.put("Zulrah", (ItemID.PET_SNAKELING_12940));
           plugin.bossIcon.put("Chambers", (ItemID.OLMLET));
           plugin.bossIcon.put("CM Chambers", (ItemID.TEKTINY));
           plugin.bossIcon.put("Gauntlet", (ItemID.YOUNGLLEF));
           plugin.bossIcon.put("Corrupted Gauntlet", (ItemID.CORRUPTED_YOUNGLLEF));
           plugin.bossIcon.put("Theater", (ItemID.LIL_ZIK));
           plugin.bossIcon.put("Vorkath", (ItemID.VORKI));
           plugin.bossIcon.put("Hydra", (ItemID.IKKLE_HYDRA_22748));
           plugin.bossIcon.put("Grotesque Guardians", (ItemID.MIDNIGHT));
           plugin.bossIcon.put("Nightmare", (ItemID.LITTLE_NIGHTMARE));
           plugin.bossIcon.put("TzTok-Jad", (ItemID.TZREKJAD));
           plugin.bossIcon.put("TzKal-Zuk", (ItemID.TZREKZUK));
           plugin.bossIcon.put("Giant Mole", (ItemID.BABY_MOLE));
           plugin.bossIcon.put("Sarachnis", (ItemID.SRARACHA));
           plugin.bossIcon.put("Abyssal Sire", (ItemID.ABYSSAL_ORPHAN));
           plugin.bossIcon.put("Zilyana", (ItemID.PET_ZILYANA));
           plugin.bossIcon.put("General Graardor", (ItemID.PET_GENERAL_GRAARDOR));
           plugin.bossIcon.put("Kree'arra", (ItemID.PET_KREEARRA));
           plugin.bossIcon.put("K'ril Tsutsaroth", (ItemID.PET_KRIL_TSUTSAROTH));
           plugin.bossIcon.put("Kraken", (ItemID.PET_KRAKEN));
           plugin.bossIcon.put("Thermy", (ItemID.PET_SMOKE_DEVIL));
           plugin.bossIcon.put("Cerberus", (ItemID.HELLPUPPY));
           plugin.bossIcon.put("King Black Dragon", (ItemID.PRINCE_BLACK_DRAGON));
           plugin.bossIcon.put("Scorpia", (ItemID.SCORPIAS_OFFSPRING));
           plugin.bossIcon.put("Chaos Fanatic", (ItemID.ANCIENT_STAFF));
           plugin.bossIcon.put("Crazy Archaeologist", (ItemID.FEDORA));
           plugin.bossIcon.put("Chaos Elemental", (ItemID.PET_CHAOS_ELEMENTAL));
           plugin.bossIcon.put("Vet'ion", (ItemID.VETION_JR_13180));
           plugin.bossIcon.put("Venenatis", (ItemID.VENENATIS_SPIDERLING));
           plugin.bossIcon.put("Barrows", (ItemID.BARROWS_TELEPORT));
           plugin.bossIcon.put("Deranged Archaeologist", (ItemID.UNIDENTIFIED_RARE_FOSSIL));
           plugin.bossIcon.put("Kalphite Queen", (ItemID.KALPHITE_PRINCESS));
           plugin.bossIcon.put("Corporeal Beast", (ItemID.PET_CORPOREAL_CRITTER));
           plugin.bossIcon.put("Dagannoth Prime", (ItemID.PET_DAGANNOTH_PRIME));
           plugin.bossIcon.put("Dagannoth Rex", (ItemID.PET_DAGANNOTH_REX));
           plugin.bossIcon.put("Dagannoth Supreme", (ItemID.PET_DAGANNOTH_SUPREME));
           plugin.bossIcon.put("Dagannoth Kings", (ItemID.DAGANNOTH));
           
           //Adding display boss list
           plugin.displayBosses.add("Zulrah");
           plugin.displayBosses.add("Vorkath");
           plugin.displayBosses.add("Hydra");
           plugin.displayBosses.add("Grotesque Guardians");
           plugin.displayBosses.add("Corrupted Gauntlet");
           plugin.displayBosses.add("Gauntlet");
           plugin.displayBosses.add("Nightmare");
           plugin.displayBosses.add("Chambers");
           plugin.displayBosses.add("CM Chambers");
           plugin.displayBosses.add("Theater");
           plugin.displayBosses.add("TzTok-Jad");
           plugin.displayBosses.add("TzKal-Zuk");
           
           //Adding Non display boss list
           plugin.noDisplayBosses.add("Giant Mole");
           plugin.noDisplayBosses.add("Sarachnis");
           plugin.noDisplayBosses.add("Abyssal Sire");
           plugin.noDisplayBosses.add("Zilyana");
           plugin.noDisplayBosses.add("General Graardor");
           plugin.noDisplayBosses.add("Kree'arra");
           plugin.noDisplayBosses.add("K'ril Tsutsaroth");
           plugin.noDisplayBosses.add("Kraken");
           plugin.noDisplayBosses.add("Thermy");
           plugin.noDisplayBosses.add("Cerberus");
           plugin.noDisplayBosses.add("King Black Dragon");
           plugin.noDisplayBosses.add("Scorpia");
           plugin.noDisplayBosses.add("Chaos Fanatic");
           plugin.noDisplayBosses.add("Crazy Archaeologist");
           plugin.noDisplayBosses.add("Chaos Elemental");
           plugin.noDisplayBosses.add("Vet'ion");
           plugin.noDisplayBosses.add("Venenatis");
           plugin.noDisplayBosses.add("Barrows");
           plugin.noDisplayBosses.add("Deranged Archaeologist");
           plugin.noDisplayBosses.add("Kalphite Queen");
           plugin.noDisplayBosses.add("Corporeal Beast");
           plugin.noDisplayBosses.add("Dagannoth Prime");
           plugin.noDisplayBosses.add("Dagannoth Rex");
           plugin.noDisplayBosses.add("Dagannoth Supreme");
           plugin.noDisplayBosses.add("Dagannoth Kings");

        }

}
