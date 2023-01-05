import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './PersonaSelect.scss';

export interface IPersonaSelectProps {
  personas: any;
  onChangePersonas: (e:any) => void;
}

const PersonaSelect = (props: IPersonaSelectProps) => {
  const [selectedPersona, setSelectedPersona] = useState([]);

  const handleOnChange = (e:any) => {
    const { value, checked } = e.target;
    if (checked) {
      setSelectedPersona([...selectedPersona, value]);
    } else {
      setSelectedPersona(selectedPersona.filter((persona:any) => persona !== value));
    }
  }

  useEffect(() => {
    props.onChangePersonas(selectedPersona);
  }, [selectedPersona]);  

  return (
    <div className={Styles.container}>
      <h2 className={Styles.heading}>Personas</h2>
      <div className={Styles.personaContainer}>
        {
          props.personas.map((persona:any) => {
            return (
              <div key={persona.id} className={Styles.persona}>
                <div className={Styles.personaAvatar}>
                  <img src={persona.avatar} />
                  <p>{persona.name}</p>
                </div>
                <div className={Styles.desc}>
                  <p>{persona.description}</p>
                </div>
                <div className={Styles.personaSelect}>
                  <label className={classNames('checkbox', Styles.checkboxItem)}>
                    <span className={classNames('wrapper', Styles.thCheckbox)}>
                      <input
                        type="checkbox"
                        className="ff-only"
                        id={persona.id}
                        value={persona.id}
                        onChange={(e) => handleOnChange(e)}
                      />
                    </span>
                  </label>
                </div>
              </div>
            )
          })
        }
      </div>
    </div>
  );
};

export default PersonaSelect;