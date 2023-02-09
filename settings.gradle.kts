
rootProject.name = "Regroup"
include("API")
include("Modules")
include("Modules:API")
findProject(":Modules:API")?.name = "API"
include("Modules:Spigot")
findProject(":Modules:Spigot")?.name = "Spigot"
