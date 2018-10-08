
file_name = "ketten-"

years = c(2010,2030)


matrices = data.frame()
for(year in years){
  path = paste(folder, "input/matrices/", file_name, year, ".csv", sep = "")
  data = fread(path, sep = ";")
  data$year = year
  data$TkmHL = as.numeric(data$TkmHL)
  matrices = rbind(matrices, data)
  rm(data)
  
}

source("analysis/load_labels.R")

matrices$ModeHL = factor(matrices$ModeHL, levels = mode_code, labels = mode_lab)
matrices$ModeVL = factor(matrices$ModeVL, levels = mode_code, labels = mode_lab)
matrices$ModeNL = factor(matrices$ModeNL, levels = mode_code, labels = mode_lab)


commodity_lab = commodities$Bezeichnung
commodity_code = commodities$NST.2007


matrices$GuetergruppeHL = factor(matrices$GuetergruppeHL,
                               levels = commodity_code,
                               labels = commodity_lab)
matrices$GuetergruppeVL = factor(matrices$GuetergruppeVL,
                                 levels = commodity_code,
                                 labels = commodity_lab)
matrices$GuetergruppeNL = factor(matrices$GuetergruppeNL,
                                 levels = commodity_code,
                                 labels = commodity_lab)

#aggregate commodities

