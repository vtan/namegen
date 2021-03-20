import * as _ from "lodash"
import * as React from "react"
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome"
import { faGenderless, faMars, faVenus } from "@fortawesome/free-solid-svg-icons"

import { Names, Sex, HistoricalNames, MarkovNames } from "./Model"
import { fetchHistoricalNames, fetchMarkovNames } from "./Logic"

export const App = () => {
  const [nameType, setNameType] = React.useState<Names["type"]>("historical")
  const [names, setNames] = React.useState<Names>()
  const [decade, setDecade] = React.useState(1990)
  const [sex, setSex] = React.useState<Sex>()
  const [bias, setBias] = React.useState<number>(1)
  const [openSelector, setOpenSelector] = React.useState<"decade" | "bias" | undefined>()

  const fetchNames = React.useMemo(() => {
    switch (nameType) {
      case "historical":
        return () => fetchHistoricalNames(decade, sex, bias, result => setNames({ type: "historical", result }))
      case "markov":
        return () => fetchMarkovNames(sex, result => setNames({ type: "markov", result }))
    }
  }, [nameType, decade, sex, bias])

  React.useEffect(() => fetchNames(), [fetchNames])

  const onDecadeClick = React.useCallback(() => {
    setOpenSelector(current => current === "decade" ? undefined : "decade")
  }, [])
  const onDecadeChange = React.useCallback(decade => {
    setDecade(decade)
    setOpenSelector(undefined)
  }, [])

  const onBiasClick = React.useCallback(() => {
    setOpenSelector(current => current === "bias" ? undefined : "bias")
  }, [])
  const onBiasChange = React.useCallback(bias => setBias(bias), [])

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
          { nameType === "historical" && <>
              <button onClick={onDecadeClick}><span className="unimportant">Born in</span> {decade}s</button>
              <button onClick={onBiasClick}>Boost rare names…</button>
          </>}
          <a href="https://github.com/vtan/namegen" className="repositoryLink">Open source</a>
        </div>
      </div>
    </header>
    <main>
      { names &&
        <div className="centeredLayout">
          { openSelector === "decade" && <DecadeSelector selected={decade} onChange={onDecadeChange} /> }
          { openSelector === "bias" && <BiasSelector selected={bias} onChange={onBiasChange} /> }
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
        return <div key={key} data-key={key} onClick={onNameClick} className={`name selectable${selectedNameKey === key ? " selected" : ""}`}>
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
      Generated from {formatNumber(result.firstNameCount)} first names
      and {formatNumber(result.lastNameCount)} last names
      based on <a href="https://github.com/vtan/namegen#data">US population data</a>.
    </p>
  </div>
}

const MarkovNameList = (props: { result: MarkovNames }) => {
  const { result } = props
  return <div className="names">
    { result.names.map((names, index) => {
        const fullName = names.join(" ")
        const key = `${index}-${fullName}`
        return <div key={key} data-key={key} className="name">
          {fullName}
        </div>
    })}
    <p>
      Generated using {formatNumber(result.firstNameRules)} probabilistic rules for first names
      and {formatNumber(result.lastNameRules)} for last names
      based on <a href="https://github.com/vtan/namegen#data">US population data</a>.
    </p>
  </div>
}

const DecadeSelector = (props: { selected: number, onChange: (_: number) => void }) => {
  const { selected, onChange } = props

  const decades = React.useMemo(() => {
    let decades = []
    for (let i = 2010; i >= 1880; i -= 10) { decades.push(i) }
    return decades
  }, [])
  const onDecadeClick = React.useCallback((e) => {
    const decade = parseInt(e.target.getAttribute("data-key"))
    onChange(decade)
  }, [onChange])

  return <div className="secondarySelector">
    { decades.map(decade =>
        <button key={decade} data-key={decade} onClick={onDecadeClick} className={selected === decade ? "selected" : ""}>
          {decade}s
        </button>
    )}
  </div>
}

const SexSelector = (props: { selected?: Sex, onChange: (_?: Sex) => void }) => {
  const { selected, onChange } = props

  const onClick = React.useCallback(e => {
    const sex = e.target.getAttribute("data-key") || undefined
    onChange(sex)
  }, [onChange])

  return <div className="sexSelector">
    <button className={ selected === undefined ? "selected" : "" } onClick={onClick}>
      <FontAwesomeIcon icon={faGenderless} />
    </button>
    <button className={ selected === "female" ? "selected"  : "" } data-key="female" onClick={onClick}>
      <FontAwesomeIcon icon={faVenus} />
    </button>
    <button className={ selected === "male" ? "selected" : "" } data-key="male" onClick={onClick}>
      <FontAwesomeIcon icon={faMars} />
    </button>
  </div>
}

const BiasSelector = (props: { selected: number, onChange: (_: number) => void }) => {
  const { selected, onChange } = props
  const [displayedValue, setDisplayedValue] = React.useState(selected)

  const dispatchChange = React.useCallback(
    _.throttle(onChange, 200, { leading: false }),
  [onChange])
  const onUnthrottledChange = React.useCallback(e => {
    const value = parseInt(e.target.value)
    dispatchChange(value)
    setDisplayedValue(value)
  }, [dispatchChange])

  return <div className="secondarySelector">
    Rare name bias
    <input type="range" min="1" max="9" step="2"
      value={displayedValue}
      onChange={onUnthrottledChange} />
  </div>
}

function formatNumber(n: number): string {
  return n.toLocaleString("en-US")
}
