# KPH Tracker
A simple plugin which tracks your kills per hour at various bossess.

The plugin works based off chat messages. It knows you got a kill by tracking each time the player receives a “Your kill count is” message in the chat. The plugin then keeps track of the number of times you receive that message during your session. The plugin then computes your average kill time by dividing your total session time by the number of kills that session (this means that your average kill time includes banking / downtime). The plugin then takes the average kill time in seconds and divides 3600(1 Hour) by that time giving the kills per hour, these calculations are updated upon each kill. 

The plugin categorizes bosses into two categories, bosses who display messages in chat for their kill time and bosses who do not.  For bosses who DO display their kill times in chat the plugin keeps a record of the total time spent actually killing the boss as determined by adding up all the kill times from chat. If the option “Account for Banking” is enabled the plugin will then calculate the time you spent not killing the boss during your session at the end of every kill. This is done by subtracting your total time spent actually killing the boss from your overall total session time. Current this feature is only available for bosses who display kill times in chat. If “Account for Banking” is not turned on the plugin will calculate your kills per hour only based off time spent actually killing the boss and will not take idle time into account. 

For bosses who DO NOT display kill times in chat the plugin will always account for banking and currently there is no way to not have idle time accounted for.

Chat Commands:
!info- This will output your current (or previous if no current session) sessions info into the chatbox.
!end- This will end your session
!pause- This will pause your session
!resume- This will resume your session
