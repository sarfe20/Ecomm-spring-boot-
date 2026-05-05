const initialState = {
    products: null,
    externalProducts: [],
    externalProductsLoading: false,
    categories: null,
    pagination: {},
};

export const productReducer = (state = initialState, action) => {
    switch (action.type) {
        case "FETCH_PRODUCTS":
            return {
                ...state,
                products: action.payload,
                pagination: {
                    ...state.pagination,
                    pageNumber: action.pageNumber,
                    pageSize: action.pageSize,
                    totalElements: action.totalElements,
                    totalPages: action.totalPages,
                    lastPage: action.lastPage,
                },
            };

        case "FETCH_PRODUCTS":
            return {
                ...state,
                products: action.payload,
                pagination: {
                    ...state.pagination,
                    pageNumber: action.pageNumber,
                    pageSize: action.pageSize,
                    totalElements: action.totalElements,
                    totalPages: action.totalPages,
                    lastPage: action.lastPage,
                },
            };

        case "EXTERNAL_PRODUCTS_LOADING":
            return {
                ...state,
                externalProductsLoading: true,
            };

        case "FETCH_EXTERNAL_PRODUCTS":
            return {
                ...state,
                externalProducts: action.payload,
            };

        case "EXTERNAL_PRODUCTS_DONE":
            return {
                ...state,
                externalProductsLoading: false,
            };
    
        case "FETCH_CATEGORIES":
            return {
                ...state,
                categories: action.payload,
                pagination: {
                    ...state.pagination,
                    pageNumber: action.pageNumber,
                    pageSize: action.pageSize,
                    totalElements: action.totalElements,
                    totalPages: action.totalPages,
                    lastPage: action.lastPage,
                },
            };
        
    
        default:
            return state;
    }
};
