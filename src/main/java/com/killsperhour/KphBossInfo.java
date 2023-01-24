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
import okhttp3.Challenge;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

enum KphBossInfo
{


    //display type 1 = yes , 0 = no Phantom Muspah
    PHANTOM_MUSPAH("Phantom Muspah", ItemID.MUPHIN,1,-1,0,"Your Phantom Muspah kill count is:"),
    ZULRAH("Zulrah", ItemID.PET_SNAKELING_12940,1,-1,0,"Your Zulrah kill count is:"),
    CHAMBERS("Chambers",ItemID.OLMLET,1,-1,1,"Your completed Chambers of Xeric count is:"),
    CM_CHAMBERS("CM Chambers",ItemID.TEKTINY,1,-1,1,"Your completed Chambers of Xeric Challenge Mode count is:"),

    TOA_NORMAL("TOA Normal",ItemID.TUMEKENS_GUARDIAN,1,-1,1,"Your completed Tombs of Amascut count is:"),
    TOA_EXPERT("TOA Expert",ItemID.AKKHITO,1,-1,1,"Your completed Tombs of Amascut: Expert Mode count is:"),



    GAUNTLET("Gauntlet",ItemID.YOUNGLLEF,1,-1,1,"Your Gauntlet completion count is:"),
    CORRUPTED_GAUNTLET("Corrupted Gauntlet",ItemID.CORRUPTED_YOUNGLLEF,1,-1,1,"Your Corrupted Gauntlet completion count is:"),
    THEATER_OF_BLOOD("Theatre of Blood",ItemID.LIL_ZIK,1,-1,1,"Your completed Theatre of Blood count is:"),

    THEATER_OF_BLOOD_HM("Theatre of Blood HM",ItemID.LIL_SOT,1,-1,1,"Your completed Theatre of Blood: Hard Mode count is:"),

    VORKATH("Vorkath",ItemID.VORKI,1,-1,0,"Your Vorkath kill count is:"),
    HYDRA("Alchemical Hydra",ItemID.IKKLE_HYDRA_22748,1,-1,0,"Your Alchemical Hydra kill count is:"),
    GROTESQUE_GUARDIANS("Grotesque Guardians",ItemID.NOON,1,-1,1,"Your Grotesque Guardians kill count is:"),

    NIGHTMARE("Nightmare",ItemID.LITTLE_NIGHTMARE,1,-1,0,"Your Nightmare kill count is:"),
    PHOSANIS_NIGHTMARE("Phosani's Nightmare",ItemID.PARASITIC_EGG,1,-1,0,"Your Phosani's Nightmare kill count is:"),

    TZTOK_JAD("TzTok-Jad",ItemID.TZREKJAD,1,-1,0,"Your TzTok-Jad kill count is:"),
    TZKAL_ZUK("TzKal-Zuk",ItemID.TZREKZUK,1,-1,0,"Your TzKal-Zuk kill count is:"),

    //times are in ticks, 120 = 1.2min , 500 = 5min, 200 = 2min, 300 = 3min ect... 100ticks = 1min

    //non-display below
    GIANT_MOLE("Giant Mole",ItemID.BABY_MOLE,0,220,-1,"Your Giant Mole kill count is:"),
    SARACHNIS("Sarachnis",ItemID.SRARACHA,0,220,-1,"Your Sarachnis kill count is:"),
    ABYSSAL_SIRE("Abyssal Sire",ItemID.ABYSSAL_ORPHAN,0,300,-1,"Your Abyssal Sire kill count is:"),
    COMMANDER_ZILYANA("Commander Zilyana",ItemID.PET_ZILYANA,0,120,-1,"Your Commander Zilyana kill count is:"),
    GENERAL_GRAARDOR("General Graardor",ItemID.PET_GENERAL_GRAARDOR,0,120,-1,"Your General Graardor kill count is:"),
    KREEARRA("Kree'arra",ItemID.PET_KREEARRA,0,120,-1,"Your Kree'arra kill count is:"),
    KRIL_TSUTSAROTH("K'ril Tsutsaroth",ItemID.PET_KRIL_TSUTSAROTH,0,120,-1,"Your K'ril Tsutsaroth kill count is:"),

    NEX("Nex",ItemID.NEXLING,1,-1,0,"Your Nex kill count is:"),

    KRAKEN("Kraken",ItemID.PET_KRAKEN,0,220,-1,"Your Kraken kill count is:"),
    ENORMOUS_TENTACLE("Enormous Tentacle",ItemID.PET_KRAKEN,0,220,-1,"not included"),
    THERMY("Thermy",ItemID.PET_SMOKE_DEVIL,0,220,-1,"Your Thermonuclear Smoke Devil kill count is:"),
    THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear smoke devil",ItemID.PET_SMOKE_DEVIL,0,220,-1,"duplicate identifier"),
    CERBERUS("Cerberus",ItemID.HELLPUPPY,0,150,-1,"Your Cerberus kill count is:"),
    KING_BLACK_DRAGON("King Black Dragon",ItemID.PRINCE_BLACK_DRAGON,0,120,-1,"Your King Black Dragon kill count is:"),
    SCORPIA("Scorpia",ItemID.SCORPIAS_OFFSPRING,0,250,-1,"Your Scorpia kill count is:"),
    CHAOS_FANATIC("Chaos Fanatic",ItemID.ANCIENT_STAFF,0,250,-1,"Your Chaos Fanatic kill count is:"),
    CALLISTO("Callisto",ItemID.CALLISTO_CUB,0,250,-1,"Your Callisto kill count is:"),

    CRAZY_ARCHAEOLOGIST("Crazy archaeologist",ItemID.FEDORA,0,250,-1,"Your Crazy Archaeologist kill count is:"),
    CHAOS_ELEMENTAL("Chaos Elemental",ItemID.PET_CHAOS_ELEMENTAL,0,250,-1,"Your Chaos Elemental kill count is:"),

    VETION("Vet'ion",ItemID.VETION_JR_13180,0,350,-1,"Your Vet'ion kill count is:"),
    VETION_REBORN("Vet'ion Reborn",ItemID.VETION_JR_13180,0,350,-1,"duplicate identifier"),

    VENENATIS("Venenatis",ItemID.VENENATIS_SPIDERLING,0,250,-1,"Your Venenatis kill count is:"),
    BARROWS("Barrows",ItemID.BARROWS_TELEPORT,0,1500,-1,"Your Barrows chest count is:"),
    VERAC_THE_DEFILED("Verac the Defiled",ItemID.BARROWS_TELEPORT,0,1500,-1,"not included"),
    TORAG_THE_CORRUPTED("Torag the Corrupted",ItemID.BARROWS_TELEPORT,0,1500,-1,"not included"),
    KARIL_THE_TAINTED("Karil the Tainted",ItemID.BARROWS_TELEPORT,0,1500,-1,"not included"),
    GUTHAN_THE_INFESTED("Guthan the Infested",ItemID.BARROWS_TELEPORT,0,1500,-1,"not included"),
    DHAROK_THE_WRETCHED("Dharok the Wretched",ItemID.BARROWS_TELEPORT,0,1500,-1,"not included"),
    AHRIM_THE_BLIGHTED("Ahrim the Blighted",ItemID.BARROWS_TELEPORT,0,1500,-1,"not included"),
    DERANGED_ARCHAEOLOGIST("Deranged archaeologist",ItemID.UNIDENTIFIED_RARE_FOSSIL,0,120,-1,"Your Deranged Archaeologist kill count is:"),
    KALPHITE_QUEEN("Kalphite Queen",ItemID.KALPHITE_PRINCESS,0,400,-1,"Your Kalphite Queen kill count is:"),
    CORPOREAL_BEAST("Corporeal Beast", ItemID.PET_CORPOREAL_CRITTER,0,400,-1,"Your Corporeal Beast kill count is:"),
    DAGANNOTH_PRIME("Dagannoth Prime",ItemID.PET_DAGANNOTH_PRIME,0,15,-1,"not included"),
    DAGANNOTH_REX("Dagannoth Rex",ItemID.PET_DAGANNOTH_REX,0,15,-1,"not included"),
    DAGANNOTH_SUPREME("Dagannoth Supreme",ItemID.PET_DAGANNOTH_SUPREME,0,15,-1,"not included"),
    DAGANNOTH_KINGS("Dagannoth Kings",ItemID.DAGANNOTH,0,15,-1,"not included");


    private final int icon;
    private final String name;
    private final String kcIdentifier;
    private final int displayType;
    private final int attkTimeout;
    private final int displayFirst;


    KphBossInfo(String name, int icon, int displayType, int attkTimeout,int displayFirst, String kcIdentifier)
    {
        this.icon = icon;
        this.name = name;
        this.displayType = displayType;
        this.attkTimeout = attkTimeout;
        this.kcIdentifier = kcIdentifier;
        this.displayFirst = displayFirst;
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

    public static final Map<String, String> bossByWords = new HashMap<String, String>();

    public static final Map<String, String> bossByWordsLoot = new HashMap<String, String>();

    public static final ArrayList<String> timeMessages = new ArrayList<String>();

    static
    {
        //duration is not necessary as duration will never come first, fight duration comes first for garg boss.
        timeMessages.add("Fight duration:");
        timeMessages.add("Congratulations - your raid is complete!");
        timeMessages.add("Corrupted challenge duration:");
        timeMessages.add("Challenge duration:");
        timeMessages.add("Theatre of Blood total completion time: ");
        timeMessages.add("Tombs of Amascut: Expert Mode total completion time:");
        timeMessages.add("Tombs of Amascut total completion time:");//normal mode
    }

    static
    {
        bossByWordsLoot.putAll(bossByWords);
        bossByWordsLoot.put("Thermonuclear smoke devil","Thermy");
        bossByWordsLoot.put("Vet'ion Reborn","Vet'ion");
        bossByWordsLoot.put("Chambers of Xeric","Chambers");
        bossByWordsLoot.put("The Gauntlet","Gauntlet");
        bossByWordsLoot.put("The Nightmare","Nightmare");
    }

    static
    {

        //used for boss name check as-well as when searching for a record

        bossByWords.put("c gauntlet","Corrupted Gauntlet");
        bossByWords.put("vork","Vorkath");

        //GG's ByWords
        bossByWords.put("garg boss","Grotesque Guardians");


        bossByWords.put("Dusk","Grotesque Guardians");


        bossByWords.put("dawn","Grotesque Guardians");
        bossByWords.put("ggs","Grotesque Guardians");




        bossByWords.put("zuk","TzKal-Zuk");
        bossByWords.put("jad","TzTok-Jad");
        bossByWords.put("mole","Giant Mole");
        bossByWords.put("sire","Abyssal Sire");

        //Sara ByWords
        bossByWords.put("sara","Commander Zilyana");
        bossByWords.put("zilly","Commander Zilyana");
        bossByWords.put("zilyana","Commander Zilyana");

        //Bandos ByWords
        bossByWords.put("bandos","General Graardor");
        bossByWords.put("graardor","General Graardor");

        //Arma ByWords
        bossByWords.put("arma","Kree'arra");
        bossByWords.put("kree","Kree'arra");
        bossByWords.put("kreearra","Kree'arra");

        //Zammy ByWords
        bossByWords.put("zammy","K'ril Tsutsaroth");
        bossByWords.put("kril","K'ril Tsutsaroth");
        bossByWords.put("kril tsutsaroth","K'ril Tsutsaroth");

        bossByWords.put("Thermonuclear smoke devil","Thermy");
        bossByWords.put("thermonuclear smoke devil","Thermy");
        bossByWords.put("cerb","Cerberus");
        bossByWords.put("kbd","King Black Dragon");
        bossByWords.put("chaos elly","Chaos Elemental");

        bossByWords.put("pnm","Phosani's Nightmare");

        bossByWords.put("Vet'ion Reborn","Vet'ion");
        bossByWords.put("vetion","Vet'ion");

        bossByWords.put("kq","Kalphite Queen");
        bossByWords.put("corp","Corporeal Beast");


        bossByWords.put("phantom","Phantom Muspah");
        bossByWords.put("muspah","Phantom Muspah");
        bossByWords.put("grumbler","Phantom Muspah");

        //CM ByWords
        bossByWords.put("cm","CM Chambers");
        bossByWords.put("cm cox","CM Chambers");
        bossByWords.put("challange mode","CM Chambers");
        bossByWords.put("challange mode chambers of xeric","CM Chambers");

        //Chambers ByWords
        bossByWords.put("chambers of xeric","Chambers");
        bossByWords.put("cox","Chambers");

        //TOB ByWords
        bossByWords.put("theatre","Theatre of Blood");
        bossByWords.put("tob","Theatre of Blood");

        //TOB HM ByWords
        bossByWords.put("theatre hm","Theatre of Blood HM");
        bossByWords.put("tob hm","Theatre of Blood HM");
        bossByWords.put("hm","Theatre of Blood HM");

        //TOA byWords
        bossByWords.put("TOA","TOA Normal");


        bossByWords.put("hydra","Alchemical Hydra");
    }



    public String getName()
    {
        return name;
    }

    public String getKcIdentifier()
    {
        return kcIdentifier;
    }

    public int getDisplayFirst()
    {
        return displayFirst;
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
