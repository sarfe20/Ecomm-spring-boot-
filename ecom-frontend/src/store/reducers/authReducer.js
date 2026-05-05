const initialState = {
    user: null,
    address: [],
    razorpayOrder: null,
    selectedUserCheckoutAddress: null,
}

export const authReducer = (state = initialState, action) => {
    switch (action.type) {
        case "LOGIN_USER":
            return { ...state, user: action.payload };
        case "USER_ADDRESS":
            return { ...state, address: action.payload };
        case "SELECT_CHECKOUT_ADDRESS":
            return { ...state, selectedUserCheckoutAddress: action.payload };
        case "REMOVE_CHECKOUT_ADDRESS":
            return { ...state, selectedUserCheckoutAddress: null };
        case "RAZORPAY_ORDER":
            return { ...state, razorpayOrder: action.payload };
        case "REMOVE_PAYMENT_CHECKOUT_DATA":
            return { ...state, razorpayOrder: null, selectedUserCheckoutAddress: null };
        case "LOG_OUT":
            return { 
                user: null,
                address: [],
                razorpayOrder: null,
                selectedUserCheckoutAddress: null,
             };
             
        default:
            return state;
    }
};
