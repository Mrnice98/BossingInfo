package com.killsperhour;


import lombok.Setter;
import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.List;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Singleton
public class FileReadWriter
{

    KphPlugin plugin;

    Client client;

    KphBossGoalsOverlay goalsOverlay;


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

    public void fetchLookupInfo()
    {
        File mainFolder = new File(RUNELITE_DIR,"bossing-info");
        file = new File(mainFolder,client.getUsername());

        filename = filename + ".txt";
        System.out.println(filename);
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

            plugin.getPanel().startKcModel.setValue(startKc);

            if(endKc > totalBossKc)
            {
                plugin.getPanel().endKcModel.setValue(endKc);
            }
            else
            {
                plugin.getPanel().endKcModel.setValue(totalBossKc + 1);
            }

            plugin.getPanel().startKcModel.setMaximum(totalBossKc);
            plugin.getPanel().endKcModel.setMinimum(totalBossKc + 1);

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


    int totalBossKc;
    int startKc;
    int endKc;

    public void resetStartAndEndKc()
    {
        startKc = 0;
        endKc = 0;
    }


    public void createFileForBoss()
    {
        boss = plugin.currentBoss;
        fileName = boss + ".txt";

        //this finds the file at the directed path
        path = Paths.get(file.getPath(), fileName);


        try
        {

            List<String> list = Files.readAllLines(path);
            list.forEach(line -> list.toArray());

            oldTotalTime = Integer.parseInt(list.get(0).replaceAll("[^0-9]", ""));
            oldTotalVirtualTime = Integer.parseInt(list.get(1).replaceAll("[^0-9]", ""));
            oldTotalKills = Integer.parseInt(list.get(2).replaceAll("[^0-9]", ""));
            oldFastestKill = Integer.parseInt(list.get(3).replaceAll("[^0-9]", ""));
            totalBossKc = Integer.parseInt(list.get(4).replaceAll("[^0-9]", ""));

            if (list.size() == 7)
            {
                startKc = Integer.parseInt(list.get(5).replaceAll("[^0-9]", ""));
                endKc = Integer.parseInt(list.get(6).replaceAll("[^0-9]", ""));
            }
            else
            {
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append(0);
                contentBuilder.append(" Start Kc\n");
                contentBuilder.append(0);
                contentBuilder.append(" End Kc\n");
                String content = contentBuilder.toString();
                Files.write(path, content.getBytes(), StandardOpenOption.APPEND);
            }

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



    }


    public void replaceAndUpdate()
    {

        //issue is that they are 2x'ing up when being sent on kill calc. need to find a way to only send once
        System.out.println(path.toString());
        System.out.println(oldTotalTime + " old");
        System.out.println(plugin.lastKillTotalTime_1 + " last kill total time");
        System.out.println(newTotalTimeActual + " new total time");
        System.out.println(startKc + " start");
        System.out.println(endKc + " end");

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





