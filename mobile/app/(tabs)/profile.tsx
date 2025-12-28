import React from 'react';
import { View, Text, ScrollView, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';
import { Button } from '@/src/components/ui/Button';
import { useAuth } from '@/src/hooks/useAuth';

export default function ProfileScreen() {
  const router = useRouter();
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    router.replace('/(auth)/login');
  };

  return (
    <View className="flex-1 bg-red-50">
      <ScrollView className="flex-1 px-6 py-6">
        <View className="bg-white rounded-2xl p-6 shadow-sm mb-4">
          <View className="items-center mb-6">
            <View className="w-20 h-20 bg-primary-600 rounded-full items-center justify-center mb-3">
              <Text className="text-white text-3xl">👤</Text>
            </View>
            <Text className="text-2xl font-bold text-gray-800">{user?.fullName}</Text>
            <Text className="text-gray-500 mt-1">{user?.email}</Text>
          </View>

          <View className="border-t border-gray-200 pt-4">
            <View className="flex-row justify-between mb-3">
              <Text className="text-gray-600">Role</Text>
              <Text className="font-semibold text-gray-800">{user?.role}</Text>
            </View>
            <View className="flex-row justify-between mb-3">
              <Text className="text-gray-600">User ID</Text>
              <Text className="font-semibold text-gray-800">#{user?.id}</Text>
            </View>
            <View className="flex-row justify-between">
              <Text className="text-gray-600">Status</Text>
              <Text className="font-semibold text-green-600">
                {user?.isActive ? 'Active' : 'Inactive'}
              </Text>
            </View>
          </View>
        </View>

        <View className="bg-white rounded-2xl p-4 shadow-sm mb-4">
          <TouchableOpacity className="flex-row items-center py-3 border-b border-gray-100">
            <Text className="text-xl mr-3">⚙️</Text>
            <Text className="text-gray-700 flex-1">Settings</Text>
            <Text className="text-gray-400">›</Text>
          </TouchableOpacity>
          
          <TouchableOpacity className="flex-row items-center py-3 border-b border-gray-100">
            <Text className="text-xl mr-3">🔔</Text>
            <Text className="text-gray-700 flex-1">Notifications</Text>
            <Text className="text-gray-400">›</Text>
          </TouchableOpacity>
          
          <TouchableOpacity className="flex-row items-center py-3">
            <Text className="text-xl mr-3">❓</Text>
            <Text className="text-gray-700 flex-1">Help & Support</Text>
            <Text className="text-gray-400">›</Text>
          </TouchableOpacity>
        </View>

        <View className="px-4">
          <Button
            title="Logout"
            variant="outline"
            onPress={handleLogout}
          />
        </View>
      </ScrollView>
    </View>
  );
}
