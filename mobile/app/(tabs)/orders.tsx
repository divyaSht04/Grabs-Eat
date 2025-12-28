import React from 'react';
import { View, Text, ScrollView } from 'react-native';

export default function OrdersScreen() {
  return (
    <View className="flex-1 bg-red-50">
      <ScrollView className="flex-1 px-6 py-6">
        <View className="bg-white rounded-2xl p-6 shadow-sm mb-4">
          <Text className="text-2xl font-bold text-gray-800 mb-2">Orders</Text>
          <Text className="text-gray-600">Your order history will appear here.</Text>
        </View>

        <View className="bg-white rounded-2xl p-6 shadow-sm">
          <Text className="text-gray-500 text-center py-8">
            📋 No orders yet
          </Text>
        </View>
      </ScrollView>
    </View>
  );
}
