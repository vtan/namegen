import { HistoricalNames, Sex, MarkovNames } from "./Model";

export function fetchHistoricalNames(
  decade: number,
  sex: Sex | undefined,
  bias: number,
  setter: (_: HistoricalNames) => void
) {
  fetch(
    `/api/names/historical?limit=20&decade=${decade}` +
      (sex ? `&sex=${sex}` : "") +
      (bias ? `&bias=${bias}` : "")
  )
    .then((response) => response.json())
    .then(setter);
}

export function fetchMarkovNames(
  sex: Sex | undefined,
  setter: (_: MarkovNames) => void
) {
  fetch(`/api/names/markov?limit=20` + (sex ? `&sex=${sex}` : ""))
    .then((response) => response.json())
    .then(setter);
}
