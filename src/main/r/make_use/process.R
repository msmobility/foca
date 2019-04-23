pacman::p_load(data.table, dplyr, tidyr)

#procces make use tables

#read code conversions

folder = "c:/projects/radLast/data/input_output/eurostat/data/selected_data/"

industries_conversion_file = "industries_conversion.csv" 
industries_conversion = read.csv(paste(folder, industries_conversion_file, sep = ""))


commodities_conversion_file = "commodities_conversion.csv" 
commodities_conversion = read.csv(paste(folder, commodities_conversion_file, sep = ""))

#merge tables (make table, 1 of 2)

supply_table_de = merge(x = supply_table_de, by.x = "induse",
                        y = industries_conversion, by.y = "industrial_uses")

supply_table_de = merge(x = supply_table_de, by.x = "prod_na",
                        y = commodities_conversion, by.y = "products")
##subset columns

st_de_short = supply_table_de %>% select(industry = silo_code_text, commodity = code, value = "2015")
st_de_short$value = as.numeric(st_de_short$value)

st_de_short = st_de_short %>% filter(industry != "-1", commodity != -1, !is.na(value)) 

st_de_short = st_de_short %>%
  group_by(industry, commodity) %>%
  summarize (value = mean(value)) %>%
  spread(commodity, value)

write.table(st_de_short, file = "clipboard", sep = "\t", row.names = F)

#merge tables (use table, 2 of 2)

use_table_de = merge(x = use_table_de, by.x = "induse",
                        y = industries_conversion, by.y = "industrial_uses")

use_table_de = merge(x = use_table_de, by.x = "prod_na",
                        y = commodities_conversion, by.y = "products")
##subset columns

ut_de_short = use_table_de %>% select(industry = silo_code_text, commodity = code, value = "2015")
ut_de_short$value = as.numeric(ut_de_short$value)

ut_de_short = ut_de_short %>% filter(industry != "-1", commodity != -1, !is.na(value)) 

ut_de_short = ut_de_short %>%
  group_by(industry, commodity) %>%
  summarize (value = mean(value)) %>%
  spread(commodity, value)

write.table(ut_de_short, file = "clipboard", sep = "\t", row.names = F)

