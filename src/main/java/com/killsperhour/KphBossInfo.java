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

import com.google.common.collect.ImmutableMap;
import net.runelite.api.ItemID;

import java.util.Map;

enum KphBossInfo
{


    //display type 1 = yes , 0 = no
    ZULRAH("Zulrah",ItemID.PET_SNAKELING_12940,1,-1),
    CHAMBERS("Chambers",ItemID.OLMLET,1,-1),
    CM_CHAMBERS("CM Chambers",ItemID.TEKTINY,1,-1),
    GAUNTLET("Gauntlet",ItemID.YOUNGLLEF,1,-1),
    CORRUPTED_GAUNTLET("Corrupted Gauntlet",ItemID.CORRUPTED_YOUNGLLEF,1,-1),
    THEATER_OF_BLOOD("Theater",ItemID.LIL_ZIK,1,-1),
    VORKATH("Vorkath",ItemID.VORKI,1,-1),
    HYDRA("Hydra",ItemID.IKKLE_HYDRA_22748,1,-1),
    GROTESQUE_GUARDIANS("Grotesque Guardians",ItemID.NOON,1,-1),
    NIGHTMARE("Nightmare",ItemID.LITTLE_NIGHTMARE,1,-1),
    TZTOK_JAD("TzTok-Jad",ItemID.TZREKJAD,1,-1),
    TZKAL_ZUK("TzKal-Zuk",ItemID.TZREKZUK,1,-1),

    //times are in ticks, 120 = 1.2min , 500 = 5min, 200 = 2min, 300 = 3min ect... 100ticks = 1min

    //non-display below
    GIANT_MOLE("Giant Mole",ItemID.BABY_MOLE,0,120),
    SARACHNIS("Sarachnis",ItemID.SRARACHA,0,120),



    ABYSSAL_SIRE("Abyssal Sire",ItemID.ABYSSAL_ORPHAN,0,300),



    COMMANDER_ZILYANA("Commander Zilyana",ItemID.PET_ZILYANA,0,120),
    GENERAL_GRAARDOR("General Graardor",ItemID.PET_GENERAL_GRAARDOR,0,120),
    KREEARRA("Kree'arra",ItemID.PET_KREEARRA,0,120),
    KRIL_TSUTSAROTH("K'ril Tsutsaroth",ItemID.PET_KRIL_TSUTSAROTH,0,120),

    KRAKEN("Kraken",ItemID.PET_KRAKEN,0,120),
    ENORMOUS_TENTACLE("Enormous Tentacle",ItemID.PET_KRAKEN,0,120),

    THERMY("Thermy",ItemID.PET_SMOKE_DEVIL,0,120),
    THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear smoke devil",ItemID.PET_SMOKE_DEVIL,0,120),

    CERBERUS("Cerberus",ItemID.HELLPUPPY,0,40),

    KING_BLACK_DRAGON("King Black Dragon",ItemID.PRINCE_BLACK_DRAGON,0,120),
    SCORPIA("Scorpia",ItemID.SCORPIAS_OFFSPRING,0,120),
    CHAOS_FANATIC("Chaos Fanatic",ItemID.ANCIENT_STAFF,0,120),

    CALLISTO("Callisto",ItemID.CALLISTO_CUB,0,120),

    CRAZY_ARCHAEOLOGIST("Crazy Archaeologist",ItemID.FEDORA,0,120),
    CHAOS_ELEMENTAL("Chaos Elemental",ItemID.PET_CHAOS_ELEMENTAL,0,120),
    VETION("Vet'ion",ItemID.VETION_JR_13180,0,120),
    VETION_REBORN("Vet'ion Reborn",ItemID.VETION_JR_13180,0,120),
    VENENATIS("Venenatis",ItemID.VENENATIS_SPIDERLING,0,120),

    BARROWS("Barrows",ItemID.BARROWS_TELEPORT,0,1500),
    VERAC_THE_DEFILED("Verac the Defiled",ItemID.BARROWS_TELEPORT,0,1500),
    TORAG_THE_CORRUPTED("Torag the Corrupted",ItemID.BARROWS_TELEPORT,0,1500),
    KARIL_THE_TAINTED("Karil the Tainted",ItemID.BARROWS_TELEPORT,0,1500),
    GUTHAN_THE_INFESTED("Guthan the Infested",ItemID.BARROWS_TELEPORT,0,1500),
    DHAROK_THE_WRETCHED("Dharok the Wretched",ItemID.BARROWS_TELEPORT,0,1500),
    AHRIM_THE_BLIGHTED("Ahrim the Blighted",ItemID.BARROWS_TELEPORT,0,1500),

    DERANGED_ARCHAEOLOGIST("Deranged Archaeologist",ItemID.UNIDENTIFIED_RARE_FOSSIL,0,120),

    KALPHITE_QUEEN("Kalphite Queen",ItemID.KALPHITE_PRINCESS,0,300),
    CORPOREAL_BEAST("Corporeal Beast", ItemID.PET_CORPOREAL_CRITTER,0,300),

    DAGANNOTH_PRIME("Dagannoth Prime",ItemID.PET_DAGANNOTH_PRIME,0,15),
    DAGANNOTH_REX("Dagannoth Rex",ItemID.PET_DAGANNOTH_REX,0,15),
    DAGANNOTH_SUPREME("Dagannoth Supreme",ItemID.PET_DAGANNOTH_SUPREME,0,15),
    DAGANNOTH_KINGS("Dagannoth Kings",ItemID.DAGANNOTH,0,15);


    private final int icon;
    private final String name;
    private final int displayType;
    private final int attkTimeout;


    KphBossInfo(String name, int icon, int displayType, int attkTimeout)
    {
        this.icon = icon;
        this.name = name;
        this.displayType = displayType;
        this.attkTimeout = attkTimeout;
    }



    static
    {
        ImmutableMap.Builder<String, KphBossInfo> builder = new ImmutableMap.Builder<>();

        for (KphBossInfo kphBossInfo : values())
        {
            builder.put(kphBossInfo.getName(), kphBossInfo);
        }
        bosses = builder.build();
    }

    public static final Map<String, KphBossInfo> bosses;


    public String getName()
    {
        return name;
    }

    public int getDisplayType()
    {
        return displayType;
    }

    public int getAttkTimeout()
    {
        return attkTimeout;
    }

    public int getIcon()
    {
        return icon;
    }

    public static KphBossInfo find(String name)
    {
        return bosses.get(name);
    }


}
