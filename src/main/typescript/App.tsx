import * as React from "react"

import { Names, Sex, HistoricalNames, MarkovNames } from "./Model"
import { fetchHistoricalNames, fetchMarkovNames } from "./Logic"

export const App = () => {
  const [nameType, setNameType] = React.useState<Names["type"]>("historical")
  const [names, setNames] = React.useState<Names>()
  const [decade, setDecade] = React.useState(1990)
  const [sex, setSex] = React.useState<Sex>()
  const [decadeSelectorOpen, setDecadeSelectorOpen] = React.useState(false)

  const fetchNames = React.useMemo(() => {
    switch (nameType) {
      case "historical":
        return () => fetchHistoricalNames(decade, sex, result => setNames({ type: "historical", result }))
      case "markov":
        return () => fetchMarkovNames(sex, result => setNames({ type: "markov", result }))
    }
  }, [nameType, decade, sex])

  React.useEffect(() => fetchNames(), [fetchNames])

  const onDecadeClick = React.useCallback(() => {
    setDecadeSelectorOpen(current => !current)
  }, [])
  const onDecadeChange = React.useCallback(decade => {
    setDecade(decade)
    setDecadeSelectorOpen(false)
  }, [])

  const onTypeChange = React.useCallback(e => {
    const nameType = e.target.getAttribute("data-type")
    setNameType(nameType)
  }, [])

  return <>
    <header>
      <div className="centeredLayout">
        <h1>Name generator</h1>
        <div className="typeSelector">
          <a data-type="historical" onClick={onTypeChange} className={ nameType === "historical" ? "selected" : "" }>Historical</a>
          <a data-type="markov" onClick={onTypeChange} className={ nameType === "markov" ? "selected" : "" }>Recombined</a>
        </div>
        <div className="selectors">
          <button onClick={fetchNames} className="generate">Generate</button>
          <SexSelector selected={sex} onChange={setSex} />
          { nameType === "historical" &&
              <button onClick={onDecadeClick}><span className="unimportant">Born in</span> {decade}s</button>
          }
          <a href="https://github.com/vtan/namegen" className="repositoryLink">Open source</a>
        </div>
      </div>
    </header>
    <main>
      { names &&
        <div className="centeredLayout">
          <DecadeSelector selected={decade} hidden={!decadeSelectorOpen} onChange={onDecadeChange} />
          { names.type === "historical" && <HistoricalNameList result={names.result} /> }
          { names.type === "markov" && <MarkovNameList result={names.result} /> }
        </div>
      }
    </main>
  </>
}

const HistoricalNameList = (props: { result: HistoricalNames }) => {
  const { result } = props
  const [selectedNameKey, setSelectedNameKey] = React.useState<string>()

  const onNameClick = React.useCallback(e => {
    const key = e.target.getAttribute("data-key")
    setSelectedNameKey(current => key === current ? undefined : key)
  }, [])

  return <div className="names">
    { result.names.map((names, index) => {
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
      Generated from {result.firstNameCount.toLocaleString("en-US")} first names
      and {result.lastNameCount.toLocaleString("en-US")} last names
      based on <a href="https://github.com/vtan/namegen#data">US population data</a>.
    </p>
  </div>
}

const MarkovNameList = (props: { result: MarkovNames }) => {
  const { result } = props
  return <div className="names">
    { result.map((names, index) => {
        const fullName = names.join(" ")
        const key = `${index}-${fullName}`
        return <div key={key} data-key={key} className="name">
          {fullName}
        </div>
    })}
    <p>
      Recombined from names
      based on <a href="https://github.com/vtan/namegen#data">US population data</a>.
    </p>
  </div>
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
