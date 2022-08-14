## New Console
A mimdustry mod adding a new console ui,
which is superiour to that provided by the vanilla game
or any other mod.

## V7
Starting from NC-1.4.1, NewConsole requires mindustry v136 or never to run.
If you're still on v135.2, you can visit the "releases" page and download a v1.4 or an older version,
which are compatible with that version.

This mod __does not__ support v6. v126 is outdated by several years,
get real!

## Main features
* A new console ui, consisting of 3 main elements:
    * A scrollable log output on the left, which also includes the scripts you've ran and their outputs, and which can be cleared.
    * An input area on the right, in which you're supposed to enter your code.
    * An action panel on top of the input area, which lets you do the following:
        * Execute the inputted code using the "run" button (both the code and the result will appear in the log).
        * Navigate in the console history (see below) using the "prev" and "next" buttons.
	* Clear the log buffer (if you want to read what's been cleared before, you can execute `backread()`).
        * Access one of the advances features (see below).

### Advanced feafures
* A menu allowing you to save scripts, load them, execute immediately, view their code, etc.
    * In order to save a script, input it in the console, type its name in the top bar and press "save". If you attempt to override a existing script, a confirmation dialog will be shown.
    * You can execute a saved script by pressing the green triangle button, copy it to the console using the yellow clipboard button, or delete it using the red trash bin button.

* A file browser. Pretty much self-explainatory, you can browse directories, open files (text ones can be copied to the console, images can be viewed, zip files can be browsed like normal directories.
    * On android 10+, its functionality is limited due to the limitations of the OS.

* A menu allowing you to execute a script when an event occurs.
    * Pretty similar to the scripts menu.
    * In the left top corner you there's a menu. You need to input the script in the console and select an event upon which it will ran.
    * You can delete a saved autorun script by pressing the red trash bin button.
    * Due to how dangerous this is, all saves autorun scripts are disabled when you restart the game to avoid a possible softlock. You need to manually re-enable them after that.


### Console history
The console history enables you to view the scripts you've executed since the launch of the game.
If you're familiar with some shells (such as bash, fish, zsh), you will easily understand how this works.

Whenever you execute a script or navigate in the history, your current script is saved on the top of the history (unless it already was on the top).

Using the "prev" and "next" buttons, you can view older and newer entries of the history.
If you press "next" while already being at the newest history entry, your current input is cleared. 
You can always press "prev" to return to ahatever you've been writing.

# Stale development
Since this mod is written in java and i'm using kotlin, i can't really develop this mod due to my hate towards java.
Things that take 1 line in kotlin sometimes take 50 in java, i can't stand writing megabytes of boilerplate code for simple things.

However, i can't just switch this mod to kotlin as that would require me to include the kolin stdlib, which takes 2 mb in the compressed size.
That would increase the size of the mod from 200 kb to 2500 and would worsen the loading times, which is not something i want to do with this mod.
