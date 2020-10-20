export interface GeneratedNames {
  firstNameCount: number,
  lastNameCount: number,
  names: ReadonlyArray<FullName>
}

export type FullName = ReadonlyArray<GeneratedName>

export interface GeneratedName {
  cumulatedProbability: number,
  name: string
}

export type Sex = "female" | "male"
