# KPH Tracker
A simple plugin which tracks your kills per hour at various bossess.

-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


Chat Commands:

!info- This will output your current (or previous if no current session) sessions info into the chatbox.

!end- This will end your session

!pause- This will pause your session

!resume- This will resume your session


-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


Config Options:

Account for Banking- 
If enabled the plugin will account for banking / idle time when doing its calculations (changing this option will end your session upon the next kill)

Output Info- 
If enabled when your session ends or switches the plugin will output a chat message to your chatbox with your last sessions information.

Session Timeout- 
The amount of time in minutes you want your session to stay valid for after your last kill. The timer resets each kill and setting the timeout to 0 will disable it all together.

KPH Calc- 
allows you to select the way in which your kills per hour is calculated.

  Precise = the exact KPH to one decimal place
  
  Rounded = KPH rounded to the nearest whole number
  
  Round up = KPH always rounded up to the next whole number
  
  Traditional = Integer division (Rounded down)

Dagannoth Selector- 
Allows you to choose which DK you want the plugin to track (Kings tracks all three) 


-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------



Description of how the plugin works:

The plugin works based off chat messages. It knows you got a kill by tracking each time the player receives a “Your kill count is” message in the chat. The plugin then keeps track of the number of times you receive that message during your session. The plugin then computes your average kill time by dividing your total session time by the number of kills that session (this means that your average kill time includes banking / downtime). The plugin then takes the average kill time in seconds and divides 3600(1 Hour) by that time giving the kills per hour, these calculations are updated upon each kill. 

The plugin categorizes bosses into two categories, bosses who display messages in chat for their kill time and bosses who do not.  For bosses who DO display their kill times in chat the plugin keeps a record of the total time spent actually killing the boss as determined by adding up all the kill times from chat. If the option “Account for Banking” is enabled the plugin will then calculate the time you spent not killing the boss during your session at the end of every kill. This is done by subtracting your total time spent actually killing the boss from your overall total session time. Current this feature is only available for bosses who display kill times in chat. If “Account for Banking” is not turned on the plugin will calculate your kills per hour only based off time spent actually killing the boss and will not take idle time into account. 

For bosses who DO NOT display kill times in chat the plugin will always account for banking and currently there is no way to not have idle time accounted for.

