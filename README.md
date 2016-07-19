# DiscordMusic
A discord music bot.

## How To Use
This is a wiki on how to use it's command. Installation is below.


## Installation

Currently not on any repositories.
You can clone this repository to fork the bot.

### Examples

#### Create a command
Class:
```java
    public class MyCommand extends GenericCommand 
    {
        public MyCommand()
        {
        }
    
        public String getAlias()
        {
            return "myAlias";
        }
    
        public void invoke()
        {
            // My Code
        }
    
        public boolean isPrivate() // optional override(default true)
        {
            return true;
        }
    }
```

Register Command:
```java
    new MusicBot(manager -> // callback containing CommandManager
    {
        manager.registerCommand(new MyCommand()); // register command
    }, 1, new Config("Base.json", true));
```

## Requirements
1. A Bot Account.
2. Python 3.5+
3. Java 1.8+
4. [DV8FromTheWorld/JDA](https://github.com/DV8FromTheWorld/JDA)
5. [DV8FromTheWorld/JDA-Player](https://github.com/DV8FromTheWorld/JDA-Player)
