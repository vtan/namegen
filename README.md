# namegen

Generates random names based on US population data. In addition to picking names with real-world probabilities, less realistic names can be generated with a Markov chain.

## Data

The first names are aggregated from yearly data back to the 1880s: https://www.ssa.gov/oact/babynames/limits.html

The last names are from the 2000 US census data: https://www.census.gov/topics/population/genealogy/data/2000_surnames.html

## Build

Extract the [first name](https://www.ssa.gov/oact/babynames/names.zip) and [last name](http://www2.census.gov/topics/genealogy/2000surnames/names.zip) data to the `data/raw` subdirectory, then generate the dataset:

```
$ sbt console
scala> namegen.historical.Dataset.buildToFile("data/firstnames.csv", "data/lastnames.csv")
scala> namegen.markov.Dataset.buildToFile("data/markov.csv")
```

Then you can start the server with `sbt run` and the client dev server with `yarn start`, which will run at http://localhost:8080/.
