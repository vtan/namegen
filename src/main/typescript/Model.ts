export type Names =
  { type: "historical", result: HistoricalNames }
  | { type: "markov", result: MarkovNames }

export interface HistoricalNames {
  firstNameCount: number,
  lastNameCount: number,
  names: ReadonlyArray<FullName>
}

export interface MarkovNames {
  firstNameRules: number,
  lastNameRules: number,
  names: ReadonlyArray<ReadonlyArray<string>>
}

export type FullName = ReadonlyArray<GeneratedName>

export interface GeneratedName {
  cumulatedProbability: number,
  name: string
}

export type Sex = "female" | "male"
