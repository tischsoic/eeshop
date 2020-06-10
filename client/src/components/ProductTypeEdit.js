import React from 'react';

import ElementEdit from './ElementEdit';

export default function ProductTypeEdit() {
  const mapElementToFields = (productType) => {
    return {
      name: productType?.name || '',
      description: productType?.description || '',
    };
  };

  return (
    <ElementEdit
      elementType="productType"
      title="Product Type"
      mapElementToFields={mapElementToFields}
      renderFormFields={({ fields, onChange, isDuringProcessing }) => (
        <>
          <div className="form-group">
            <label htmlFor="name">Name</label>
            <input
              type="text"
              className="form-control"
              id="name"
              name="name"
              placeholder="Name"
              value={fields.name}
              onChange={onChange}
              disabled={isDuringProcessing}
            />
          </div>
          <div className="form-group">
            <label htmlFor="description">Description</label>
            <input
              type="text"
              className="form-control"
              id="description"
              name="description"
              placeholder="Description"
              value={fields.description}
              onChange={onChange}
              disabled={isDuringProcessing}
            />
          </div>
        </>
      )}
    />
  );
}
