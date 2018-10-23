pacman::p_load(data.table, ggplot2, dplyr, reshape)

folder = "c:/models/freightFlows/"

source("input/input_zones.R")
source("input/input_commodities.R")
source("input/input_matrices.R")


source("analysis/main_summary.R")



source("analysis/plot_summaries_by_zones.R")
