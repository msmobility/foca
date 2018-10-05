path_to_file = "input/commodities/nst2007.csv"

commodities = read.csv(paste(folder, path_to_file, sep = ""), sep=";")

commodities$Bezeichnung = as.character(commodities$Bezeichnung)

commodities = rbind(commodities, list(NST.2007 = 0, Bezeichnung = "Nich besetzt"))
