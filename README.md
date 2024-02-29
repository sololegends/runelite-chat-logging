# Chat Logging
This allows logging of messages within different channels to various files depending on message channel. 

Different channels can be toggled on and off individually. Log files are rotated on a daily basis with with a custom number of files to be kept.

## Log Storage 
Logs are stores in Runelite's home directory, `chatlogs` directory, then a directory by logged in user, then by chat message source.  
Runelite's home directory is `%userprofile%\.runelite` on Windows or `~/.runelite` on Linux or macOS.

Example: 
```
.runelite/
└── chatlogs/
  └── <username>/
      ├── friends/
      ├── private/
      ├── public/
      └── clan/
      └── group/
      └── game/
```  



## Thanks 

Special thanks to [hex-agon](https://github.com/hex-agon) for laying the groundwork for this plugin