import { GeneratedNames, Sex } from "./Model"

export function fetchNames(
  decade: number,
  sex: Sex | undefined,
  setter: (_: GeneratedNames) => void
) {
  fetch(`/api/names?limit=20&decade=${decade}` + (sex ? `&sex=${sex}` : ""))
    .then(response => response.json())
    .then(setter)
}
