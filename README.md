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


## Requirements
1. A Bot Account.
2. Python 3.5+
3. Java 1.8+