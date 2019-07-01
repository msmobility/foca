pacman::p_load(data.table, dplyr, tidyr, sf, ggplot2, readr)

folder = "c:/projects/radLast/data/zensus/"

# n_pp_file = "Zensus_Bevoelkerung_100m-Gitter.csv"
# 
# n_pp = read_csv2(paste(folder, n_pp_file, sep =""))
# 
# x_min = 4461831
# x_max = 4489630
# y_min = 2869351
# y_max = 2891972
# 
# n_pp = n_pp %>% filter(x_mp_100m > x_min, x_mp_100m < x_max, y_mp_100m > y_min, y_mp_100m < y_max)
# 
# bb_file = "Wohnungen100m.csv"
# bb = read_csv(paste(folder, bb_file, sep = ""))
