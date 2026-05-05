import React from 'react'
import { FaCheckCircle } from 'react-icons/fa';

const PaymentConfirmation = () => {
  return (
    <div className='min-h-screen flex items-center justify-center'>
            <div className="p-8 rounded-lg shadow-lg text-center max-w-md mx-auto border border-gray-200">
                <div className="text-green-500 mb-4 flex  justify-center">    
                    <FaCheckCircle size={64} />
                </div>
                <h2 className='text-3xl font-bold text-gray-800 mb-2'>Payment Successful!</h2>
                <p className="text-gray-600 mb-6">
                    Thank you for your purchase! Your payment was successful, and we’re
                    processing your order.
                </p>
            </div>
    </div>
  )
}

export default PaymentConfirmation
