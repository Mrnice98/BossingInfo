# KPH Tracker
A simple plugin which tracks your kills per hour at various bossess.  

Please note this plugin is in its first iteration :small_blue_diamond: (Beta) and there is likely to be a bug here and there as its difficult to test all scenarios on every boss. **PLEASE** let me know if you find any bugs, it will be very much appreciated!   

I have a number of features I plan on adding and over the coming weeks including improved support and calculation methods for non-time displaying bosses.  If there is anything you think would be a good addition to the plugin feel free to join the [Discord](https://discord.gg/ATXSsbbXQV) and send me a message.  

#### Support:   
The best way to get support, report an issue or give a suggestion is to join the [Discord](https://discord.gg/ATXSsbbXQV)!  
Alternatively feel free to leave a comment or open an issue on GitHub


-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

### **__Supported Bosses:__**                                                                                                                                                                                                                                                                                                   
- Currently the plugin has full support for the following bosses:  
  `Zulrah`   `Vorkath`   `Nightmare`   `Theater of Blood`   `Chambers of Xeric & CM`   `Alchemical Hydra`   `Grotesque Guardians`   `The Gauntlet & Corrupted`   `TzTok-Jad`    `Thermonuclear smoke devil`  `Cerberus`   `Chaos Fanatic`   `Crazy archaeologist`   `Scorpia`   `King Black Dragon`  `Chaos Elemental`  `Venenatis`   `Callisto`  `Giant Mole`   `Dagannoth Kings`   `Sarachnis`   `Kalphite Queen`   `Kree'arra`   `Commander Zilyana`   `General Graardor`   `K'ril   Tsutsaroth`   `Corporeal Beast` `Kraken`  `Abyssal Sire`  `Vet'ion`  `Barrows`  
  
- Newly added:  
  `Kraken`  `Abyssal Sire`  `Vet'ion`  `Barrows`  
     **These bosses are newly added and may not work perfectly*  
       
- Currently The plugin does not have support for:  
 `Wintertodt`   `Zalcano`   `Penance Queen`
 
 -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

### Chat Commands:
- !info- This will output your current (or previous if no current session) sessions info into the chatbox.  
- !end- This will end your session  
- !pause- This will pause your session  
- !resume- This will resume your session


-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

### Config Options:

- **Display Options**

  - Eanble Overlay-   
  Enables the box overlay.                                          
  ![Imgur Image](https://i.imgur.com/L3oqkuw.png)
  
  - Display Infobox-   
  If enabled a Infobox will display with your KPH in it, Hovering over the infobox will show your full session stats.  
  ![Imgur Image](https://i.imgur.com/3GEBNXG.png)

  - Average kill time-  
  If enabled plugin will display your average kill time.
  
  - Fastest kill time-  
  If enabled plugin will display your fastest kill time.
  
  - Kills this Session-  
  If enabled plugin will display your kills this session.

  - Session Time-  
  If enabled plugin will display current session time.
  
  - Idle Time-  
  If enabled plugin will display your idle time.


- **General Settings**

  - Side Panel-
  Enables or Disables the side panel (changes take effect when client is restarted)   
  ![Imgur Image](https://i.imgur.com/1grrmmS.png)
    - Actual-
    this is your bossing info clculated with idle time taken into account.
    - Virtual-
    This is your bossing info calculated without idle time taken into account.
  
   - Side Panel Position-   
  Change the postion of the side panel icon on the client sidebar. Lower #'s will move it up, Higher #'s will move it down.  

  - Output Info-   
  If enabled when your session ends or switches the plugin will output a chat message to your chatbox with your last sessions information.  
  
  - Kill Duration-  
  If enabled when killing a Non-Display boss the plugin will calculate and output your exact kill time to the chatbox  
  ![Imgur Image](https://i.imgur.com/KXmGvnw.png)

  - Session Timeout-   
  The amount of time in minutes you want your session to stay valid for after your last kill. The timer resets each kill and setting the timeout to 0 will disable it all      together.

  - KPH Calc-   
  allows you to select the way in which your kills per hour is calculated.  

    - Precise = the exact KPH to one decimal place
    - Rounded = KPH rounded to the nearest whole number
    - Round up = KPH always rounded up to the next whole number  
    - Traditional = Integer division (Rounded down)


  - Dagannoth Selector-   
  Allows you to choose which DK you want the plugin to track (Kings tracks all three) 


-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


### Change Log / Updates:  
- **Version 1.1**   
    - Complete revamp of the way kill times are collected and calculated for Non-Display bosses.  
    - Addition of an optional Side Panel  
    - Addition of an optional Info Box display  
    - Addition of optional kill time tracking and fight duration output for bosses which would not ordinarily display times in chat  
    - Numerous small changes to naming conventions and minor fixes
    
- **Version 1.2**   
    - Add Barrows, Vetion, Sire.
    - Improve tracking for kraken
    - Fix Thmery & Zilly
    - fixed issue where kills would not register correctly when teleporting out at same time as kill
    - a few backend fixes to make way for the next update
    
- **Version 1.3**   
    - Fix barrows.
    - Fix issue where if "set boss kill count message as spam" setting was enabled plugin would not work.
    - Change panel layout, and buttons. 
    - Pause and resume are now one button.
    - Add button to switch between virtual and actual calculation info.
    - Add auto pause upon log out
    - numerous backend changes to better optimize code 
    
- **Version 1.4**   
    - Add boss-info logging.
    - Add boss-info lookup panel, allows you to check your boss info overtime.
    - Revamp of the kcIdentification system. 
    - numerous backend changes to better optimize code 
    

-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
Description of how the plugin works: **Re Write Needed**

The plugin works based off chat messages. It knows you got a kill by tracking each time the player receives a “Your kill count is” message in the chat. The plugin then keeps track of the number of times you receive that message during your session. The plugin then computes your average kill time by dividing your total session time by the number of kills that session (this means that your average kill time includes banking / downtime). The plugin then takes the average kill time in seconds and divides 3600(1 Hour) by that time giving the kills per hour, these calculations are updated upon each kill. 

The plugin categorizes bosses into two categories, bosses who display messages in chat for their kill time and bosses who do not.  ~~For bosses who DO display their kill times in chat the plugin keeps a record of the total time spent actually killing the boss as determined by adding up all the kill times from chat. If the option “Account for Banking” is enabled the plugin will then calculate the time you spent not killing the boss during your session at the end of every kill. This is done by subtracting your total time spent actually killing the boss from your overall total session time. Current this feature is only available for bosses who display kill times in chat. If “Account for Banking” is not turned on the plugin will calculate your kills per hour only based off time spent actually killing the boss and will not take idle time into account.~~ 

~~For bosses who DO NOT display kill times in chat the plugin will always account for banking and currently there is no way to not have idle time accounted for.~~

~~For bosses which display kill times in chat the plugin is able to account for your first kill by getting its time from the chat. For bosses who do not display kill time in chat the plugin assumes that the time it takes for you to get your second kill is roughly as long as your first kill would have taken and amends the total time accordingly. I plan to update / change the way this works for non-display bosses in the near future.~~

