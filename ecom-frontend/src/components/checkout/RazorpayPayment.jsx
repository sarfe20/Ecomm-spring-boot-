import { Alert, AlertTitle, Skeleton } from '@mui/material';
import React, { useMemo, useState } from 'react';
import toast from 'react-hot-toast';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { createRazorpayOrder, razorpayPaymentConfirmation } from '../../store/actions';

const RAZORPAY_SCRIPT_URL = 'https://checkout.razorpay.com/v1/checkout.js';

const loadRazorpayScript = () =>
  new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }

    const script = document.createElement('script');
    script.src = RAZORPAY_SCRIPT_URL;
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });

const RazorpayPayment = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [isProcessing, setIsProcessing] = useState(false);
  const { totalPrice } = useSelector((state) => state.carts);
  const { isLoading, errorMessage } = useSelector((state) => state.errors);
  const { user, selectedUserCheckoutAddress } = useSelector((state) => state.auth);

  const paymentAmount = useMemo(() => Math.round(Number(totalPrice || 0) * 100), [totalPrice]);

  const handlePayment = async () => {
    if (!selectedUserCheckoutAddress?.addressId) {
      toast.error('Please select a checkout address.');
      return;
    }

    if (!paymentAmount || paymentAmount <= 0) {
      toast.error('Cart total must be greater than zero.');
      return;
    }

    setIsProcessing(true);
    const scriptLoaded = await loadRazorpayScript();

    if (!scriptLoaded) {
      setIsProcessing(false);
      toast.error('Unable to load Razorpay checkout. Please try again.');
      return;
    }

    const razorpayOrder = await dispatch(
      createRazorpayOrder({
        amount: paymentAmount,
        currency: 'INR',
        email: user?.email,
        name: user?.username,
        address: selectedUserCheckoutAddress,
        description: `Order for ${user?.email}`,
      })
    );

    if (!razorpayOrder?.id) {
      setIsProcessing(false);
      return;
    }

    const options = {
      key: razorpayOrder.key,
      amount: razorpayOrder.amount,
      currency: razorpayOrder.currency,
      name: 'Ecommerce Store',
      description: 'Order payment',
      order_id: razorpayOrder.id,
      prefill: {
        name: user?.username || '',
        email: user?.email || '',
      },
      notes: {
        addressId: selectedUserCheckoutAddress.addressId,
      },
      handler: async (response) => {
        await dispatch(
          razorpayPaymentConfirmation(
            {
              addressId: selectedUserCheckoutAddress.addressId,
              pgName: 'Razorpay',
              pgOrderId: response.razorpay_order_id,
              pgPaymentId: response.razorpay_payment_id,
              pgSignature: response.razorpay_signature,
              pgStatus: 'succeeded',
              pgResponseMessage: `Razorpay order ${response.razorpay_order_id} completed`,
            },
            toast,
            navigate
          )
        );
        setIsProcessing(false);
      },
      modal: {
        ondismiss: () => {
          setIsProcessing(false);
          toast.error('Payment was cancelled.');
        },
      },
      theme: {
        color: '#111827',
      },
    };

    const razorpay = new window.Razorpay(options);
    razorpay.on('payment.failed', (response) => {
      setIsProcessing(false);
      toast.error(response?.error?.description || 'Payment failed. Please try again.');
    });
    razorpay.open();
  };

  if (isLoading) {
    return (
      <div className='max-w-lg mx-auto'>
        <Skeleton />
      </div>
    );
  }

  return (
    <div className='max-w-lg mx-auto p-5 border rounded-lg bg-white'>
      {errorMessage && (
        <Alert severity='error' className='mb-4'>
          <AlertTitle>Payment Error</AlertTitle>
          {errorMessage}
        </Alert>
      )}

      <h2 className='text-xl font-semibold mb-3'>Complete Payment</h2>
      <p className='text-gray-600 mb-4'>
        You will be redirected to Razorpay to complete your payment securely.
      </p>

      <button
        type='button'
        onClick={handlePayment}
        disabled={isProcessing || isLoading}
        className='text-white w-full px-5 py-[10px] bg-black mt-2 rounded-md font-bold disabled:opacity-50 disabled:animate-pulse'
      >
        {isProcessing ? 'Processing...' : `Pay ₹${Number(totalPrice || 0).toFixed(2)}`}
      </button>
    </div>
  );
};

export default RazorpayPayment;
