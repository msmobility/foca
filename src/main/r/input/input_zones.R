
path_to_file = "input/zones.csv"
zones = read.csv(paste(folder, path_to_file, sep = ""), sep=";")

zoneType = zones$Verkehrszelle
zoneType[1:412] = "lkr"
zoneType[413:565] = "ext"
zoneType[566:601] = "see_port"

zones$type = zoneType


path_to_file = "input/terminalliste.csv"

terminals = read.csv(paste(folder, path_to_file, sep = ""), sep=";")
