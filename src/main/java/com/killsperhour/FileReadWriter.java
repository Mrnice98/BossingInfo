package com.killsperhour;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Singleton
public class FileReadWriter
{

    KphPlugin plugin;

    Client client;

    KphBossGoalsOverlay goalsOverlay;

    @Inject
    ItemManager itemManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private FileReadWriter(KphPlugin plugin, Client client)
    {
        this.plugin = plugin;
        this.client = client;
    }


    File file;
    String boss;
    String fileName;
    Path path;
    Path lookupPath;

    int oldTotalTime;
    int oldTotalKills;

    int timeOffset;
    int newTotalTimeActual;

    int virtualTimeOffset;
    int oldTotalVirtualTime;
    int newTotalVirtualTime;

    int oldFastestKill;
    int newFastestKill;



    int lastKillTimeActual;
    int lastKillTimeVirtual;


    int newTotalKills;



    public void createAndUpdate()
    {
        createDirectory();
        createFileForBoss();
        replaceAndUpdate();
        statConverter();
    }



    @Setter
    String filename;

    int fetchedTotalTimeActual;
    int fetchedTotalTimeVirtual;
    int fetchedTotalTrackedKills;
    int fetchedFastestKill;
    int fetchedTotalBossKc;
    double fetchedKillsPerHour;

    int fetchedAverageKillTime;
    int estimatedTimeSpentBossing;

    File fetchedFile;

    Gson gson = new Gson();

    Map<Integer, Integer> drops = new HashMap<Integer, Integer>();
    Map<Integer, Integer> allItemDrops = new HashMap<Integer, Integer>();
    Map<Integer, Integer> fetchedAllItemDrops = new HashMap<Integer, Integer>();
    Map<Integer, Integer> sessionItemDrops = new HashMap<Integer, Integer>();

    File lootDirectory;
    File subDirectory;
    File ignoreDirectory;


    public int idNormalizer(int itemId)
    {
        switch (itemManager.getItemComposition(itemId).getName())
        {
            case "Clue Scroll (beginner)":
                return 23182;

            case "Clue Scroll (easy)":
                return 2677;

            case "Clue scroll (medium)":
                return 2801;

            case "Clue scroll (hard)":
                return 2722;

            case "Clue scroll (elite)":
                return 12073;

            case "Clue scroll (master)":
                return 19835;

            default:
                return itemId;
        }
    }

    public void buildLootMaps(List<Integer> itemIds,  List<Integer> itemQuants, Map<Integer,Integer> lootMap)
    {
        int i;
        for(i = 0; i < itemIds.size(); i++)
        {
            int itemId = idNormalizer(itemIds.get(i));
            drops.put(itemId,itemQuants.get(i));
            if(lootMap.containsKey(itemId))
            {
                int quantity = lootMap.get(itemId);
                quantity = quantity + drops.get(itemId);
                lootMap.replace(itemId,quantity);
            }
            else
            {
                lootMap.putAll(drops);
            }
            drops.clear();
        }
    }

    boolean bossNameMatch;

    public void bossNameMatcher()
    {
        if(plugin.bossName.equals(plugin.currentBoss))
        {
            bossNameMatch = true;
        }
        else if(plugin.currentBoss.equals("CM Chambers") && plugin.bossName.equals("Chambers of Xeric"))
        {
            bossNameMatch = true;
        }
        else if(plugin.currentBoss.equals("Corrupted Gauntlet") && plugin.bossName.equals("The Gauntlet"))
        {
            bossNameMatch = true;
        }
        else if(plugin.currentBoss.equals("Dagannoth Kings") &&
               (plugin.bossName.equals("Dagannoth Rex")
             || plugin.bossName.equals("Dagannoth Prime")
             || plugin.bossName.equals("Dagannoth Supreme")))
        {
            bossNameMatch = true;
        }
        else if(KphBossInfo.bossByWordsLoot.containsKey(plugin.bossName))
        {
            plugin.bossName = KphBossInfo.bossByWordsLoot.get(plugin.bossName);
            if(plugin.bossName.equals(plugin.currentBoss))
            {
               bossNameMatch = true;
            }
        }
        else
        {
            bossNameMatch = false;
        }

    }

    public void lootReceived()
    {
        lootDirectory = new File(file, "boss-loot");
        subDirectory = new File(lootDirectory,plugin.currentBoss + ".json");

        bossNameMatcher();

        if(!bossNameMatch)
        {
            System.out.println("miss matched name");
            return;
        }

        loadDropsFromMap();

        if(!plugin.getPanel().fetchedInfoPanel.isShowing())
        {
            loadIgnoredList(plugin.currentBoss);
        }

        List<Integer> id =  plugin.itemStacks.stream().map(itemStack -> itemStack.getId()).collect(Collectors.toList());
        List<Integer> quant =  plugin.itemStacks.stream().map(itemStack -> itemStack.getQuantity()).collect(Collectors.toList());

        buildLootMaps(id,quant,allItemDrops);
        buildLootMaps(id,quant,sessionItemDrops);

        writeDropsToMap();

        itemAndTotalPrice = new HashMap<Integer, Double>();

        if(plugin.getPanel().fetchedInfoPanel.isShowing())
        {
           return;
        }

        plugin.getPanel().updateLootGrid(plugin.getPanel().lootDisplayMap());
    }

    HashMap<Integer, Double> itemAndTotalPrice;
    double totalGp = 0;

    ArrayList<Integer> ignored;

    public void getTotalPrice()
    {
        double totalGpLast = totalGp;

        for (Integer entry : itemAndTotalPrice.keySet())
        {
            if(ignored.contains(entry) && !plugin.getPanel().hideItemButton.isSelected())
            {
                continue;
            }
            totalGp += itemAndTotalPrice.get(entry);
        }
        itemAndTotalPrice.clear();
        totalGp = totalGp - totalGpLast;
    }


    public void loadFetchedDropsFromMap()
    {
        try
        {
            Type IntegerMap = new TypeToken<Map<Integer, Integer>>(){}.getType();
            fetchedAllItemDrops = gson.fromJson(new FileReader(subDirectory), IntegerMap);
        }
        catch (FileNotFoundException e)
        {
            fetchedAllItemDrops = new HashMap<Integer, Integer>();
            if(!plugin.getPanel().fetchedInfoPanel.isShowing())
            {
                try
                {
                    lootDirectory.mkdirs();
                    subDirectory.createNewFile();
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
    }



    public void loadDropsFromMap()
    {
        try
        {
            Type IntegerMap = new TypeToken<Map<Integer, Integer>>(){}.getType();
            allItemDrops = gson.fromJson(new FileReader(subDirectory), IntegerMap);
        }
        catch (FileNotFoundException e)
        {
            try
            {
                lootDirectory.mkdirs();
                subDirectory.createNewFile();
                allItemDrops = new HashMap<Integer, Integer>();
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }


    public void loadIgnoredList(String bossName)
    {
        try
        {
            Type IntegerList = new TypeToken<ArrayList<Integer>>(){}.getType();
            ignoreDirectory = new File(lootDirectory,bossName + "-ignored.json");
            ignored = gson.fromJson(new FileReader(ignoreDirectory), IntegerList);
        }
        catch (FileNotFoundException e)
        {
            ignored = new ArrayList<Integer>();
            if(!plugin.getPanel().fetchedInfoPanel.isShowing())
            {
                try
                {
                    ignoreDirectory.createNewFile();
                    writeIgnoredListToFile(plugin.currentBoss);
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
    }



    public void writeDropsToMap()
    {
        try
        {
            Writer writer = new FileWriter(subDirectory);
            gson.toJson(allItemDrops,writer);
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void writeIgnoredListToFile(String bossName)
    {
        try
        {
            ignoreDirectory = new File(lootDirectory, bossName + "-ignored.json");
            Writer writer = new FileWriter(ignoreDirectory);
            gson.toJson(ignored,writer);
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }




    // function to sort hashmap by values
    public HashMap<Integer, Integer> sortByValue(HashMap<Integer,Double> hm, Map<Integer,Integer> lootMap)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<Integer, Double> > list = new LinkedList<Map.Entry<Integer, Double> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double> >()
        {
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<Integer, Integer> sorted = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> aa : list)
        {
            sorted.put(aa.getKey(), lootMap.get(aa.getKey()));
        }
        return sorted;
    }


    public void fetchLookupInfo()
    {
        File mainFolder = new File(RUNELITE_DIR,"bossing-info");
        file = new File(mainFolder,client.getUsername());
        filename = filename + ".txt";
        lookupPath = Paths.get(file.getPath(),filename);
        fetchedFile = new File(String.valueOf(lookupPath));
        if(!fetchedFile.exists())
        {
            System.out.println("File was not found");
            return;
        }

        try
        {

            List<String> list = Files.readAllLines(lookupPath);
            list.forEach(line -> list.toArray());

            fetchedTotalTimeActual = Integer.parseInt(list.get(0).replaceAll("[^0-9]", ""));
            fetchedTotalTimeVirtual = Integer.parseInt(list.get(1).replaceAll("[^0-9]", ""));
            fetchedTotalTrackedKills = Integer.parseInt(list.get(2).replaceAll("[^0-9]", ""));
            fetchedFastestKill = Integer.parseInt(list.get(3).replaceAll("[^0-9]", ""));
            fetchedTotalBossKc = Integer.parseInt(list.get(4).replaceAll("[^0-9]", ""));
            if(list.size() == 8)
            {
                lootKillsTracked = Integer.parseInt(list.get(7).replaceAll("[^0-9]", ""));
            }

            fetchedStatConverter();
        }
        catch (IOException e)
        {
            System.out.println("No Such File");
        }

    }



    public void getCurrentBossKCGoal()
    {
        int totalBossKc;
        try
        {
            List<String> list = Files.readAllLines(path);
            list.forEach(line -> list.toArray());

            totalBossKc = Integer.parseInt(list.get(4).replaceAll("[^0-9]", ""));
            startKc = Integer.parseInt(list.get(5).replaceAll("[^0-9]", ""));
            endKc = Integer.parseInt(list.get(6).replaceAll("[^0-9]", ""));

            plugin.getPanel().startKcModel.setMaximum(totalBossKc);
            plugin.getPanel().startKcModel.setValue(totalBossKc);

            plugin.getPanel().endKcModel.setMinimum(totalBossKc);
            plugin.getPanel().endKcModel.setValue(totalBossKc);
            plugin.getPanel().endKcModel.setStepSize(5);

        }
        catch (IOException e)
        {
            System.out.println("No Such File");
        }

    }


    public void updateBossKCGoal()
    {
        try
        {

            List<String> list = Files.readAllLines(path);
            list.forEach(line -> list.toArray());

            startKc = plugin.getPanel().startKC;
            endKc = plugin.getPanel().endKC;

            list.set(5, plugin.getPanel().startKC + " Start Kc");
            list.set(6, plugin.getPanel().endKC + " End Kc");

            Files.delete(path);
            Files.write(path, list, StandardOpenOption.CREATE,StandardOpenOption.APPEND);

        }
        catch (IOException e)
        {
            System.out.println("No Such File");
        }



    }

    public void resetBossGoal()
    {
        try
        {
            List<String> list = Files.readAllLines(path);
            list.forEach(line -> list.toArray());

            list.set(5, 0 + " Start Kc");
            list.set(6, 0 + " End Kc");

            startKc = 0;
            endKc = 0;

            Files.delete(path);
            Files.write(path, list, StandardOpenOption.CREATE,StandardOpenOption.APPEND);

            plugin.getPanel().updateBossGoalsPanel();

        }
        catch (IOException e)
        {
            System.out.println("No Such File");
        }

    }







    public void createDirectory()
    {

        //should create a directoy for each login profile
        File mainFolder = new File(RUNELITE_DIR,"bossing-info");
        file = new File(mainFolder,client.getUsername());

        if(!file.exists())
        {
            file.mkdirs();
            System.out.println("creating directory");
        }
        else
        {
            System.out.println("directory in place");
        }

    }


    int lootKillsTracked;

    int totalBossKc;
    int startKc;
    int endKc;

    public void resetStartAndEndKc()
    {
        startKc = 0;
        endKc = 0;
    }

    List<String> list;

    public void populateNewDataFields()
    {
        try
        {
            StringBuilder contentBuilder = new StringBuilder();
            if(list.size() < 7)
            {
                contentBuilder.append(0);
                contentBuilder.append(" Start Kc\n");
                contentBuilder.append(0);
                contentBuilder.append(" End Kc\n");
            }
            contentBuilder.append(0);
            contentBuilder.append(" Loot Kills Tracked\n");
            String content = contentBuilder.toString();
            Files.write(path, content.getBytes(), StandardOpenOption.APPEND);

            list = Files.readAllLines(path);
            list.forEach(line -> list.toArray());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void createFileForBoss()
    {
        boss = plugin.currentBoss;
        fileName = boss + ".txt";

        //this finds the file at the directed path
        path = Paths.get(file.getPath(), fileName);

        try
        {
            list = Files.readAllLines(path);
            list.forEach(line -> list.toArray());

            if(list.size() < 8)
            {
                populateNewDataFields();
            }

            oldTotalTime = Integer.parseInt(list.get(0).replaceAll("[^0-9]", ""));
            oldTotalVirtualTime = Integer.parseInt(list.get(1).replaceAll("[^0-9]", ""));
            oldTotalKills = Integer.parseInt(list.get(2).replaceAll("[^0-9]", ""));
            oldFastestKill = Integer.parseInt(list.get(3).replaceAll("[^0-9]", ""));
            totalBossKc = Integer.parseInt(list.get(4).replaceAll("[^0-9]", ""));
            startKc = Integer.parseInt(list.get(5).replaceAll("[^0-9]", ""));
            endKc = Integer.parseInt(list.get(6).replaceAll("[^0-9]", ""));
            lootKillsTracked = Integer.parseInt(list.get(7).replaceAll("[^0-9]", ""));
        }

        catch (IOException e)
        {
            System.out.println("File being created");
            try
            {
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append(plugin.totalTime);
                contentBuilder.append(" Total Time Actual\n");
                contentBuilder.append(plugin.totalTime);
                contentBuilder.append(" Total Time Virtual\n");
                contentBuilder.append(plugin.killsThisSession);
                contentBuilder.append(" Kills Tracked\n");
                contentBuilder.append(plugin.fastestKill);
                contentBuilder.append(" Fastest Kill\n");
                contentBuilder.append(plugin.killCount);
                contentBuilder.append(" Total Kc\n");
                contentBuilder.append(0);
                contentBuilder.append(" Start Kc\n");
                contentBuilder.append(0);
                contentBuilder.append(" End Kc\n");
                contentBuilder.append(1);
                contentBuilder.append(" Loot Kills Tracked\n");

                String content = contentBuilder.toString();
                Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                nullValuesForFileCreation();

            }
            catch (IOException ioException)
            {

                System.out.println("File could not be created");
            }


        }

    }



    public void nullValuesForFileCreation()
    {
        boss = plugin.currentBoss;
        fileName = boss + ".txt";

        oldTotalTime = 0;
        oldTotalKills = 0;
        timeOffset = 0;
        newTotalTimeActual = 0;
        lastKillTimeActual = 0;

        virtualTimeOffset = 0;
        oldTotalVirtualTime = 0;
        newTotalVirtualTime = 0;

        virtualAverageKillTime = 0;
        virtualKillsPerHour = 0;

        oldFastestKill = 99999999;
        newFastestKill = 99999999;
        lastKillTimeVirtual = 0;
        lootKillsTracked = 0;
    }


    public void replaceAndUpdate()
    {

        if(plugin.isBossChatDisplay())
        {

            if(plugin.killsThisSession == 1)
            {
                newTotalVirtualTime = oldTotalVirtualTime + plugin.totalBossKillTime;
                newTotalTimeActual = oldTotalTime + plugin.lastKillTotalTime_1;
            }
            else
            {

                virtualTimeOffset = plugin.totalBossKillTime - lastKillTimeVirtual;
                timeOffset = plugin.lastKillTotalTime_1 - lastKillTimeActual;

                newTotalTimeActual = newTotalTimeActual + timeOffset;
                newTotalVirtualTime = newTotalVirtualTime + virtualTimeOffset;
            }

            newTotalKills = oldTotalKills + 1;

            lastKillTimeActual = plugin.lastKillTotalTime_1;
            lastKillTimeVirtual = plugin.totalBossKillTime;

        }
        else
        {
            if(plugin.killsThisSession == 1)
            {

                newTotalVirtualTime = oldTotalVirtualTime + plugin.totalKillTime;
                newTotalTimeActual = oldTotalTime + plugin.lastKillTotalTime_0;

            }
            else
            {
                virtualTimeOffset = plugin.totalKillTime - lastKillTimeVirtual;
                timeOffset = plugin.lastKillTotalTime_0 - lastKillTimeActual;

                newTotalTimeActual = newTotalTimeActual + timeOffset;
                newTotalVirtualTime = newTotalVirtualTime + virtualTimeOffset;

            }
            newTotalKills = oldTotalKills + 1;
            lastKillTimeActual = plugin.lastKillTotalTime_0;
            lastKillTimeVirtual = plugin.totalKillTime;
        }

        if(plugin.fastestKill < oldFastestKill && oldFastestKill != 0)
        {
            newFastestKill = plugin.fastestKill;
        }
        else
        {
            newFastestKill = oldFastestKill;
        }

        lootKillsTracked = lootKillsTracked + 1;

        try
        {
            Files.delete(path);

            StringBuilder contentBuilder = new StringBuilder();

            contentBuilder.append(newTotalTimeActual);
            contentBuilder.append(" Total Time Actual\n");
            contentBuilder.append(newTotalVirtualTime);
            contentBuilder.append(" Total Time Virtual\n");
            contentBuilder.append(newTotalKills);
            contentBuilder.append(" Kills Tracked\n");
            contentBuilder.append(newFastestKill);
            contentBuilder.append(" Fastest Kill\n");
            contentBuilder.append(plugin.killCount);
            contentBuilder.append(" Total Kc\n");
            contentBuilder.append(startKc);
            contentBuilder.append(" Start Kc\n");
            contentBuilder.append(endKc);
            contentBuilder.append(" End Kc\n");
            contentBuilder.append(lootKillsTracked);
            contentBuilder.append(" Loot Kills Tracked\n");

            String content = contentBuilder.toString();
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE,StandardOpenOption.APPEND);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }



    }



    int averageKillTime;
    double killsPerHour;

    int virtualAverageKillTime;
    double virtualKillsPerHour;
    int overallTime;



    public void statConverter()
    {

        if(plugin.calcMode == 0)
        {
            overallTime = newTotalTimeActual;
        }
        else
        {
            overallTime = newTotalVirtualTime;
        }

        averageKillTime = overallTime / newTotalKills;

        if(averageKillTime == 0)
        {
            killsPerHour = 0;
        }
        else
        {
            virtualKillsPerHour = 3600D / virtualAverageKillTime;
            killsPerHour = 3600D / averageKillTime;
        }

        DecimalFormat df = new DecimalFormat("#.#");
        killsPerHour = Double.parseDouble(df.format(killsPerHour));
    }



    public void fetchedStatConverter()
    {

        int newTime;

        if(plugin.calcMode == 0)
        {
            newTime = fetchedTotalTimeActual;
        }
        else
        {
            newTime = fetchedTotalTimeVirtual;
        }

        fetchedAverageKillTime = newTime / fetchedTotalTrackedKills;

        if(fetchedAverageKillTime == 0)
        {
            fetchedKillsPerHour = 0;
        }
        else
        {
            fetchedKillsPerHour = 3600D / fetchedAverageKillTime;
        }

        DecimalFormat df = new DecimalFormat("#.#");
        fetchedKillsPerHour = Double.parseDouble(df.format(fetchedKillsPerHour));

        estimatedTimeSpentBossing = (int) (fetchedTotalBossKc / (fetchedKillsPerHour / 3600));


    }



}





