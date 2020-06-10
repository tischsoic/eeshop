import { useReducer, useCallback } from 'react';

export const SET_DURING_PROCESSING = 'SET_DURING_PROCESSING';
export const SET_FIELD = 'SET_FIELD';
export const RESET_FIELDS = 'RESET_FIELDS';

function reducer(state, action) {
  switch (action.type) {
    case SET_FIELD:
      if (state.isDuringProcessing) {
        return state;
      }
      return {
        ...state,
        fields: { ...state.fields, [action.fieldName]: action.fieldValue },
      };
    case RESET_FIELDS:
      return {
        ...state,
        fields: action.fields,
      };
    case SET_DURING_PROCESSING:
      return { ...state, isDuringProcessing: action.isDuringProcessing };
    default:
      throw new Error();
  }
}

export default function useForm(initialFields) {
  const [{ fields, isDuringProcessing }, dispatch] = useReducer(reducer, {
    fields: initialFields,
    isDuringProcessing: false,
  });
  const onFieldChange = useCallback(
    (event) => {
      const { name, value } = event.currentTarget;
      dispatch({ type: SET_FIELD, fieldName: name, fieldValue: value });
    },
    [dispatch]
  );
  const onFieldChangeCustom = useCallback(
    (fieldName, fieldValue) => {
      dispatch({ type: SET_FIELD, fieldName, fieldValue });
    },
    [dispatch]
  );
  const setIsDuringProcessing = useCallback(
    (newIsDuringProcessing) =>
      dispatch({ type: SET_DURING_PROCESSING, isDuringProcessing: newIsDuringProcessing }),
    [dispatch]
  );
  const resetFields = useCallback(
    (newFields) => dispatch({ type: RESET_FIELDS, fields: newFields }),
    [dispatch]
  );

  return [
    fields,
    isDuringProcessing,
    onFieldChange,
    setIsDuringProcessing,
    onFieldChangeCustom,
    resetFields,
  ];
}
