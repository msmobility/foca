pacman::p_load(readr, dplyr)


data = read_csv2("c:/models/freightFlows/input/matrices/ketten-2010.csv")
data = data %>% filter(Quellzelle == 9362 | Zielzelle == 9362)


write.csv2(data, "c:/models/freightFlows/input/matrices/ketten-2010-filtered.csv", row.names = F, quote = F)
