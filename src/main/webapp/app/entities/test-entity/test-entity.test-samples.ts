import { ITestEntity, NewTestEntity } from './test-entity.model';

export const sampleWithRequiredData: ITestEntity = {
  id: 24296,
};

export const sampleWithPartialData: ITestEntity = {
  id: 22494,
};

export const sampleWithFullData: ITestEntity = {
  id: 2491,
  name: 'uniform tough',
};

export const sampleWithNewData: NewTestEntity = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
