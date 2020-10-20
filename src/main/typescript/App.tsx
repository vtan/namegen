import * as React from "react"

import { GeneratedNames, Sex } from "./Model"
import { fetchNames } from "./Logic"

export const App = () => {
  const [generated, setGenerated] = React.useState<GeneratedNames>()
  const [decade, setDecade] = React.useState(1990)
  const [sex, setSex] = React.useState<Sex>()
  const [decadeSelectorOpen, setDecadeSelectorOpen] = React.useState(false)
  const [selectedNameKey, setSelectedNameKey] = React.useState<string>()

  React.useEffect(() => {
    setSelectedNameKey(undefined)
    fetchNames(decade, sex, setGenerated)
  }, [decade, sex, setGenerated])

  const onDecadeClick = React.useCallback(() => {
    setDecadeSelectorOpen(current => !current)
  }, [])

  const onDecadeChange = React.useCallback(decade => {
    setDecade(decade)
    setDecadeSelectorOpen(false)
  }, [])

  const onGenerateClick = React.useCallback(() => {
    setSelectedNameKey(undefined)
    fetchNames(decade, sex, setGenerated)
  }, [decade, sex, setGenerated])

  const onNameClick = React.useCallback(e => {
    const key = e.target.getAttribute("data-key")
    setSelectedNameKey(current => key === current ? undefined : key)
  }, [])

  return <>
    <header>
      <div className="centeredLayout">
        <h1>Name generator</h1>
        <div className="selectors">
          <button onClick={onGenerateClick} className="generate">Generate</button>
          <button onClick={onDecadeClick}><span className="unimportant">Born in</span> {decade}s</button>
          <SexSelector selected={sex} onChange={setSex} />
          <a href="https://github.com/vtan/namegen" className="repositoryLink">Open source</a>
        </div>
      </div>
    </header>
    <main>
      { generated &&
        <div className="centeredLayout">
          <DecadeSelector selected={decade} hidden={!decadeSelectorOpen} onChange={onDecadeChange} />
          <div className="names">
            { generated.names.map((names, index) => {
                const fullName = names.map(name => name.name).join(" ")
                const key = `${index}-${fullName}`
                return <div key={key} data-key={key} onClick={onNameClick} className={`name${selectedNameKey === key ? " selected" : ""}`}>
                  {fullName}
                  { selectedNameKey === key &&
                      <div className="nameDetails">
                        { names.map((name, index) =>
                            <div key={index} className="nameDetailsRow">
                              <span>{name.name}</span>
                              <span>{ (name.cumulatedProbability * 100).toFixed(1) }% of group has a more common name</span>
                            </div>
                        )}
                      </div>
                  }
                </div>
            })}
            <p>
              Generated from {generated.firstNameCount.toLocaleString("en-US")} first names
              and {generated.lastNameCount.toLocaleString("en-US")} last names
              based on <a href="https://github.com/vtan/namegen#data">US population data.</a>
            </p>
          </div>
        </div>
      }
    </main>
  </>
}

const DecadeSelector = (props: { selected: number, hidden: boolean, onChange: (_: number) => void }) => {
  const { selected, hidden, onChange } = props

  const decades = React.useMemo(() => {
    let decades = []
    for (let i = 2010; i >= 1880; i -= 10) { decades.push(i) }
    return decades
  }, [])
  const onDecadeClick = React.useCallback((e) => {
    const decade = parseInt(e.target.getAttribute("data-key"))
    onChange(decade)
  }, [onChange])

  return <div className={`decadeSelector ${hidden ? "hidden" : ""}`}>
    { decades.map(decade =>
        <button key={decade} data-key={decade} onClick={onDecadeClick} className={selected === decade ? "selected" : ""}>
          {decade}s
        </button>
    )}
  </div>
}

const SexSelector = (props: { selected?: Sex, onChange: (_?: Sex) => void }) => {
  const { selected, onChange } = props

  const onFemaleClick = React.useCallback(
    () => onChange(selected === "female" ? undefined : "female"),
    [selected]
  )
  const onMaleClick = React.useCallback(
    () => onChange(selected === "male" ?  undefined : "male"),
    [selected]
  )

  return <div className="sexSelector">
    <button className={ selected === "male" ? "" : "selected" } onClick={onFemaleClick}>♀</button>
    <button className={ selected === "female" ? "" : "selected" } onClick={onMaleClick}>♂</button>
  </div>
}
