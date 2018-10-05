
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


