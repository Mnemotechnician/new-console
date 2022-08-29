## New Console
A Mindustry mod adding a new console ui,
which is superior to that provided by the vanilla game
or any other mod.

## V7
Starting from NC-1.4.1, NewConsole requires mindustry v136 or never to run.
If you're still on v135.2, you can visit the "releases" page and download a v1.4 or an older version,
which are compatible with that version.

This mod __does not__ support v6. v126 is outdated by several years,
get real!

# Why NewConsole?
Why prefer NewConsole over other console mods or the built-in one? Well, there are several reasons:
* NC 1.8+ has syntax highlighting, indentation guides, indentation assistance and other code editor features,
  __not__ provided by any other console mod (as of today), let alone the vanilla console.
* NC has a multi-line scrollable input field and a scrollable log, which also intercepts all logs
  and reads last_log.txt when the game launches, allowing you to know everything.
* NC has a script history, which enables you to view the scripts you've run or edited before,
  and a menu that enables you to permanently save your scripts and view/edit the saved ones,
  meaning it will never leave you worrying that you will lose "that one useful script".
* NC has many other features, most of which are listed below.

## Main features
* A new console ui, consisting of 3 main elements:
    * A scrollable log output on the left, which also includes the scripts you've run and their outputs, and which can be cleared.
    * A code editor on the right, in which you're supposed to enter your code.
    * An action panel on top of the input area, which lets you do the following:
        * Execute the current code using the "run" button (both the code and the result will appear in the log).
        * Navigate in the console history (see below) using the "prev" and "next" buttons.
	* Clear the log buffer (if you want to read what's been cleared before, you can execute `backread()`).
        * Access one of the advances features (see below).

### Advanced features
* A menu allowing you to save scripts, load them, execute immediately, view their code, etc.
    * In order to save a script, input it in the console, type its name in the top bar and press "save". If you attempt to override a existing script, a confirmation dialog will be shown.
    * You can execute a saved script by pressing the green triangle button, copy it to the console using the yellow clipboard button, or delete it using the red trash bin button.

* A file browser. Pretty much self-explanatory, you can browse directories, open files (text ones can be copied to the console, images can be viewed, zip files can be browsed like normal directories).
    * On android 10+, its functionality is limited due to the limitations of the OS.

* A menu allowing you to execute a script when an event occurs.
    * Pretty similar to the scripts menu.
    * In the left top corner you there's a menu. You need to input the script in the console and select an event upon which it will run.
    * You can delete a saved autorun script by pressing the red trash bin button.
    * Due to how dangerous this is, all saves autorun scripts are disabled when you restart the game to avoid a possible softlock. You need to manually re-enable them after that.


### Console history
The console history enables you to view the scripts you've executed since the launch of the game.
If you're familiar with some shells (such as bash, fish, zsh), you will easily understand how this works.

Whenever you execute a script or navigate in the history, your current script is saved on the top of the history (unless it already was on the top).

Using the "prev" and "next" buttons, you can view older and newer entries of the history.
If you press "next" while already being at the newest history entry, your current input is cleared. 
You can always press "prev" to return to whatever you've been writing.

# Rewrite ahead
NewConsole 1.8 will be the last version of NewConsole written in java.
Later it will be rewritten from scratch in kotlin using mkui and everything will be changed.
I already have a plan about that, but I'm not sure when I will be able to begin.
Not telling any dates yet, it will probably happen somewhere in 2023.

NeoConsole, or NewConsole 2.0, will include both the old and the new version of the mod,
allowing you to choose what you want to use, so it will not be a rough change.
