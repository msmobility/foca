path_to_file = "input/commodities/nst2007.csv"

commodities = read.csv(paste(folder, path_to_file, sep = ""), sep=";")

commodities$Bezeichnung = as.character(commodities$Bezeichnung)

commodities = rbind(commodities, list(NST.2007 = 0, Bezeichnung = "Nich besetzt"))

path_to_file = "input/commodities/commodity_groups.csv"

commodity_groups = read.csv(paste(folder, path_to_file, sep = ""), sep=",")

commodity_groups$Group = factor(commodity_groups$Group, levels = c("Agriculture and forest",
                                                                  "Soil and rock",
                                                                  "Primary", 
                                                                  "Chemical",
                                                                  "Manufactured - heavy",
                                                                  "Manufactured",
                                                                  "Food",
                                                                  "Packet",
                                                                  "Group",
                                                                  "Waste",
                                                                  "Other",
                                                                  "Empty"))
