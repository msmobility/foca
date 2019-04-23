pacman::p_load(data.table, dplyr)

#read use tables from eurostat


folder = "c:/projects/radLast/data/input_output/eurostat/data/selected_data/"

file_supply = "naio_10_cp15_edit.csv"

supply_table = fread(paste(folder, file_supply, sep = ""), header = T)


#understand the data

summary(as.factor(supply_table$stk_flow))
summary(as.factor(supply_table$induse))
summary(as.factor(supply_table$prod_na))
summary(as.factor(supply_table$geo))


industrial_uses = unique(supply_table$induse)
products = unique(supply_table$prod_na)

#export the different industries and commodities

write.table(as.data.frame(industrial_uses), file = "clipboard", sep = "\t", row.names = F)
write.table(as.data.frame(products), file = "clipboard", sep = "\t", row.names = F)


#filter data to Germany in Million EUR
supply_table_de = supply_table %>% filter(geo == "DE", unit == "MIO_EUR")

#read use tables from eurostat
file_use = "naio_10_cp16_edit.csv"
use_table = fread(paste(folder, file_use, sep = ""), header = T)

#testing the data
summary(as.factor(use_table$stk_flow))
summary(as.factor(use_table$induse))
summary(as.factor(use_table$prod_na))
summary(as.factor(use_table$geo))

#filter data to Germany in Million EUR
use_table_de = use_table %>% filter(geo == "DE", unit == "MIO_EUR")
